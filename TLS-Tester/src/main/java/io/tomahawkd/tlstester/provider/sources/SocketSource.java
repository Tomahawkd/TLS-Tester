package io.tomahawkd.tlstester.provider.sources;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.NetworkArgDelegate;
import io.tomahawkd.tlstester.provider.TargetStorage;
import io.tomahawkd.tlstester.socket.CtrlSocketDataHandler;
import io.tomahawkd.tlstester.socket.DataSocketDataHandler;
import io.tomahawkd.tlstester.socket.SocketConstants;
import io.tomahawkd.tlstester.socket.SocketData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
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
	private Deque<Future<Void>> dataResults = new ConcurrentLinkedDeque<>();
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
				server.close();
			} catch (InterruptedException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error("Failed to close server", e);
			}
		}));
	}

	@Override
	public void acquire(TargetStorage storage) {

		lock.lock();
		while (!shutdownFlag) {
			lock.unlock();
			try {

				// we need to handle control function
				// as a single thread. The executor
				// is used in handling data connection
				Socket socket = server.accept();
				handleConnection(socket, storage);
			} catch (IOException e) {
				logger.error("Exception during accepting", e);
			} finally {
				lock.lock();
			}
		}
		lock.unlock();

		// clear data processes
		while (dataResults.size() > 0) {
			try {
				dataResults.pop().get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error when wait for execution result", e);
			}
		}

		try {
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.SECONDS);
			server.close();
		} catch (IOException e) {
			logger.error("Failed to close server.");
		} catch (InterruptedException e) {
			logger.error("Failed to close executor.");
		}
	}

	private void handleConnection(Socket socket, TargetStorage storage) {
		try {
			logger.debug("New Connection from {}",
					socket.getRemoteSocketAddress().toString());

			InputStream in = new DataInputStream(
					new BufferedInputStream(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();

			int control = in.read();
			switch (control) {
				case -1:
					logger.warn("eof reached while reading control byte");
					writeStatus(out, SocketConstants.INSUFFICIENT_LENGTH);
					break;
				case SocketConstants.TYPE_CTRL:
					logger.debug("Received control command");
					handleCtrl(in, out);
					break;
				case SocketConstants.TYPE_DATA:
					logger.debug("Received data command");
					try {
						dataResults.addLast(executor.submit(() -> {
							logger.debug("executing data handling");
							handleData(in, out, storage);
							return null;
						}));
					} catch (RejectedExecutionException e) {
						logger.error("Too many connections, dropping");
					}
					break;
				default:
					logger.warn("invalid control byte");
					writeStatus(out, SocketConstants.INVALID_CONTROL_BYTE);
					break;
			}
			out.flush();
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

		byte[] ctrlCode = new byte[4];
		if (in.read(ctrlCode, 0, 4) == -1) {
			logger.warn("eof reached while reading length");
			writeStatus(out, SocketConstants.INSUFFICIENT_LENGTH);
			return;
		}

		SocketData data = new CtrlSocketDataHandler().to(ctrlCode);
		if (data.getStatus() != SocketConstants.OK) {
			writeStatus(out, data.getStatus());
			return;
		}

		switch (data.getOptionalData()) {
			case SocketConstants.CTRL_STOP: {
				logger.debug("Received stop command");
				lock.lock();
				shutdownFlag = true;
				logger.debug("Shutdown flag is set");
				lock.unlock();
				break;
			}

			default: {
				logger.warn("Unknown ctrl code");
				writeStatus(out, SocketConstants.INVALID_CONTROL_BYTE);
				break;
			}
		}

		writeStatus(out, data.getStatus());
	}

	private void handleData(InputStream in, OutputStream out, TargetStorage storage)
			throws IOException {

		byte[] overallLength = new byte[4];
		if (in.read(overallLength, 0, 4) == -1) {
			logger.warn("eof reached while reading length");
			writeStatus(out, SocketConstants.INSUFFICIENT_LENGTH);
			return;
		}
		int len = ByteBuffer.wrap(overallLength)
				.order(ByteOrder.BIG_ENDIAN).getInt();
		logger.debug("Get data length " + len);

		byte[] byteData = new byte[4 + len];
		System.arraycopy(overallLength, 0, byteData, 0, 4);
		if (in.read(byteData, 4, len) == -1) {
			logger.warn("eof reached while reading length");
			writeStatus(out, SocketConstants.INSUFFICIENT_LENGTH);

			return;
		}

		SocketData data = new DataSocketDataHandler().to(byteData);
		if (data.getStatus() != SocketConstants.OK) {
			writeStatus(out, data.getStatus());
			return;
		}

		storage.addAll(data.getData());
		writeStatus(out, data.getStatus());
	}

	private void writeStatus(OutputStream out, int status) {
		try {
			out.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
					.putInt(status).array());

			// client could ignore the response code and directly shutdown
		} catch (IOException e) {
			logger.warn("Error when writing status to client");
		}
	}
}
