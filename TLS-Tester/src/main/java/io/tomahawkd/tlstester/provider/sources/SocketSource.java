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
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Source(name = InternalNamespaces.Sources.SOCKET)
public class SocketSource extends AbstractTargetSource {

	private static final Logger logger = LogManager.getLogger(SocketSource.class);

	private ServerSocket server;
	private ThreadPoolExecutor executor;
	private boolean shutdownFlag = false;

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
		while (!shutdownFlag) {
			try {
				Socket socket = server.accept();
				executor.execute(() -> acquireData(socket, storage));
			} catch (IOException e) {
				logger.fatal("Exception during accepting", e);
			} catch (RejectedExecutionException e) {
				logger.error("Too many connections, dropping");
			}
		}
	}

	private void acquireData(Socket socket, TargetStorage storage) {
		try {
			logger.debug("New Connection from {}",
					socket.getRemoteSocketAddress().toString());

			// Data strcture is as follows:
			// +---------------------+-------------+-----------------+---------+
			// |  overall_length(4)  |           list(overall_length)          |
			// +---------------------+-------------+-----------------+---------+
			// |  overall_length(4)  |  version(1) | target_ip(16)   | port(2) |
			// +---------------------+-------------+----+------------+---------+
			// | The data overall    | ip version  | ip | ipv6 or    | target  |
			// | length              |             | v4 | blank      | port    |
			// |                     |             | 4B | 12B        |         |
			// +---------------------+-------------+-----------------+---------+
			//
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
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

				InetAddress add;
				if (data[0] == 4) {
					add = Inet4Address.getByAddress(Arrays.copyOfRange(data, 1, 5));
				} else if (data[0] == 6) {
					add = Inet6Address.getByAddress(Arrays.copyOfRange(data, 1, 17));
				} else {
					logger.error("Invalid version " + data[0]);
					out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
							.putShort(INVALID_VERSION).array());
					continue;
				}

				int port = ByteBuffer.wrap(Arrays.copyOfRange(data, 17, 19))
						.order(ByteOrder.BIG_ENDIAN).getInt();

				String target = add.getHostAddress() + ":" + port;
				logger.debug("Wrapped host: {}", target);
				storage.add(target);
				len -= 35;
			}
			out.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
					.putShort(OK).array());
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

	public static final short OK = 0x0001;
	public static final short INSUFFICIENT_LENGTH = 0x1001;
	public static final short BAD_LENGTH = 0x1002;
	public static final short INVALID_VERSION = 0x1003;
}
