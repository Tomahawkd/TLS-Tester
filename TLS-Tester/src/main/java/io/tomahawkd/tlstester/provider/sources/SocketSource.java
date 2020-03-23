package io.tomahawkd.tlstester.provider.sources;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.NetworkArgDelegate;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
@Source(name = InternalNamespaces.Sources.SOCKET)
public class SocketSource extends AbstractTargetSource {

	private static final Logger logger = LogManager.getLogger(SocketSource.class);

	private ServerSocket server;
	private ThreadPoolExecutor executor;
	private boolean shutdownFlag = false;

	private ReentrantLock lock = new ReentrantLock();

	public SocketSource(String args) {
		super(args);

		InetAddress add;
		int port;

		try {
			if (args.isEmpty()) {
				add = InetAddress.getLocalHost();
				port = 23333;
			} else if (args.contains(":")) {
				String[] l = args.split(":");
				add = InetAddress.getByName(l[0]);
				port = Integer.parseInt(l[1]);
			} else {
				add = InetAddress.getLocalHost();
				port = Integer.parseInt(args);
			}
		} catch (UnknownHostException | NumberFormatException e) {
			throw new ParameterException(e);
		}

		if (port < 0 || port > 0xFFFF) {
			throw new ParameterException("Illegal port " + port);
		}

		try {
			server = new ServerSocket(port, 50, add);
		} catch (IOException e) {
			throw new RuntimeException("Cannot create socket server");
		}

		int count =
				ArgConfigurator.INSTANCE.getByType(NetworkArgDelegate.class)
						.getNetworkThreadsCount();
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(count);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				shutdownFlag = true;
				executor.shutdownNow();
				executor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));
	}

	@Override
	public void acquire(TargetStorage storage) {
		Deque<Future<Void>> results = new ConcurrentLinkedDeque<>();

		lock.lock();
		while (!shutdownFlag) {
			lock.unlock();
			try {
				Socket socket = server.accept();
				results.push(executor.submit(() -> {
					handleConnection(socket, storage);
					return null;
				}));
			} catch (IOException e) {
				logger.fatal("Exception during accepting", e);
			} catch (RejectedExecutionException e) {
				logger.error("Too many connections, dropping");
			} finally {
				lock.lock();
			}
		}
		lock.unlock();

		while (results.size() > 0) {
			try {
				results.pop().get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error when wait for execution result");
			}
		}
	}

	private void handleConnection(Socket socket, TargetStorage storage) {
		try {
			logger.debug("New Connection from {}",
					socket.getRemoteSocketAddress().toString());

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			int control = in.read();
			if (control == -1) {
				logger.warn("eof reached while reading control byte");
				out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
						.putShort(INSUFFICIENT_LENGTH).array());
			} else if (control == 1) {
				logger.debug("Received control command");
				handleCtrl(in, out);
			} else if (control == 0) {
				logger.debug("Received data command");
				handleData(in, out, storage);
			} else {
				logger.warn("invalid control byte");
				out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
						.putShort(INVALID_CONTROL_BYTE).array());
			}
		} catch (IOException e) {
			logger.fatal("Exception during accepting", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn("Cannot close socket.");
			}
		}
	}

	private void handleCtrl(InputStream in, OutputStream out) throws IOException {

		// Data strcture is as follows:
		// +---------+--------------+
		// | type(1) | ctrl_code(2) |
		// +---------+--------------+
		// | ctrl: 1 | control code |
		// +---------+--------------+
		//
		byte[] ctrlCode = new byte[2];
		if (in.read(ctrlCode, 0, 2) == -1) {
			logger.warn("eof reached while reading length");
			out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
					.putShort(INSUFFICIENT_LENGTH).array());
			return;
		}

		short code = ByteBuffer.wrap(ctrlCode).order(ByteOrder.BIG_ENDIAN).getShort();

		switch (code) {
			case CTRL_STOP: {
				logger.debug("Received stop command");
				lock.lock();
				// if true should use confirm stop
				// if false set to true
				shutdownFlag = !shutdownFlag;
				logger.debug("Shutdown flag set to {}", shutdownFlag);
				lock.unlock();
				break;
			}

			case CTRL_CONFIRM_STOP: {
				logger.debug("Received stop confirm command");
				lock.lock();
				if (!shutdownFlag) {
					logger.warn("Invalid stop confirm command.");
					out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
							.putShort(INVALID_CONTROL_BYTE).array());
				}
				lock.unlock();
				break;
			}

			default: {
				logger.warn("Unknown ctrl code");
				out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
						.putShort(INVALID_CONTROL_BYTE).array());
			}
		}
	}

	private void handleData(InputStream in, OutputStream out, TargetStorage storage)
			throws IOException {

		// Data strcture is as follows:
		// +---------+---------------------+-------------+-----------------+---------+
		// | type(1) |  overall_length(4)  |           list(overall_length)          |
		// +---------+---------------------+-------------+-----------------+---------+
		// | type(1) |  overall_length(4)  |  version(1) | target_ip(16)   | port(2) |
		// +---------+---------------------+-------------+----+------------+---------+
		// | data: 0 | The data overall    | ip version  | ip | ipv6 or    | target  |
		// |         | length              |             | v4 | blank      | port    |
		// |         |                     |             | 4B | 12B        |         |
		// +---------+---------------------+-------------+-----------------+---------+
		//
		byte[] overallLength = new byte[4];
		if (in.read(overallLength, 0, 4) == -1) {
			logger.warn("eof reached while reading length");
			out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
					.putShort(INSUFFICIENT_LENGTH).array());
			return;
		}
		int len = ByteBuffer.wrap(overallLength)
				.order(ByteOrder.BIG_ENDIAN).getInt();

		final int packLen = 19;
		if (len % packLen != 0) {
			logger.warn("bad length");
			out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
					.putShort(BAD_LENGTH).array());
			return;
		}

		while (len > 0) {
			byte[] data = new byte[packLen];
			int l = in.read(data, 0, packLen);

			// practically, this could happen when the connection is lost
			if (l == -1) {
				logger.warn("early eof");
				return;
			} else if (l != packLen) {
				logger.error("Length mismatch: " + l);
				return;
			}

			logger.debug("Received data: {}", () -> {
				StringBuilder sb = new StringBuilder();
				for (byte b : data)
					sb.append(String.format("%02x", b));
				return sb.toString();
			});
			ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

			InetAddress add;
			// buffer.get will advance the position
			if (buffer.get() == 4) {
				byte[] byteAdd = new byte[4];
				buffer.get(byteAdd);
				add = Inet4Address.getByAddress(byteAdd);
			} else if (buffer.get() == 6) {
				byte[] byteAdd = new byte[16];
				buffer.get(byteAdd);
				add = Inet6Address.getByAddress(byteAdd);
			} else {
				logger.error("Invalid version " + data[0]);
				out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
						.putShort(INVALID_VERSION).array());
				continue;
			}
			int port = buffer.getShort(17);

			String target = add.getHostAddress() + ":" + port;
			logger.debug("Wrapped host: {}", target);
			storage.add(target);
			len -= 35;
		}
		out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
				.putShort(OK).array());
	}

	// status code
	public static final short OK = 0x1000;
	public static final short INVALID_CONTROL_BYTE = 0x1001;
	public static final short INSUFFICIENT_LENGTH = 0x1002;
	public static final short BAD_LENGTH = 0x1003;
	public static final short INVALID_VERSION = 0x1004;

	// control code
	public static final short CTRL_STOP = 0x0002;
	public static final short CTRL_CONFIRM_STOP = 0x0003;
}
