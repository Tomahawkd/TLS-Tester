package io.tomahawkd.netservice;

import com.fooock.shodan.ShodanRestApi;
import com.fooock.shodan.model.host.Host;
import com.fooock.shodan.model.host.HostReport;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShodanQueriesHelper {

	private static final Logger logger = Logger.getLogger(ShodanQueriesHelper.class);

	private static final String path = FileHelper.TEMP + "/shodan/query/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create shodan directory");
		}
	}


	private static ShodanRestApi api;

	static {
		try {
			api = new ShodanRestApi(FileHelper.readFile("./keys/shodan_key"));
			checkCredits();
		} catch (IOException e) {
			logger.fatal("Error on loading api file");
		} catch (IllegalArgumentException e) {
			logger.fatal("Error on creating api");
			System.err.println(e.getMessage());
		}
	}

	private static synchronized void checkCredits() {
		try {
			api.info().subscribe(e -> {
				int credits = e.getQueryCredits();
				logger.info("You have " + credits + " credits");
				if (credits <= 0) {
					logger.fatal("No more credits(" + credits + ")");
					throw new IllegalArgumentException("No more credits(" + credits + ")");
				}
			}, error -> {
				logger.critical("Error occurs when reading credits");
				logger.critical(error.getMessage());
			}).dispose();
		} catch (Exception e) {
			logger.critical("Error occurs when reading credits");
			logger.critical(e.getMessage());
		}

		// shodan limit api request rate to 1s
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {
		}
	}

	public static List<String> searchIpWithSerial(String serial) throws Exception {

		if (serial.trim().isEmpty()) return new ArrayList<>();

		String file = path + serial + extension;
		logger.debug("IP file: " + file);

		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			IpObserver observer = new IpObserver();
			DisposableObserver<HostReport> adaptor =
					new DisposableObserverAdapter<HostReport>().add(observer).add(DEFAULT_HOSTREPORT_LOGGER);

			searchWithSerial(serial, adaptor);
			while (!observer.isComplete()) {
				try {
					logger.info("Not complete, sleeping");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.warn("Got interrupted");
					break;
				}
			}

			return observer.toString();
		});

		return CommonParser.parseHost(data);
	}

	public static void searchWithSerial(String serial, DisposableObserver<HostReport> observer) {
		searchWith("ssl.cert.serial:" + serial, observer);
	}

	public static void searchWithIp(@NotNull String ip, DisposableObserver<Host> observer) {

		checkCredits();

		ip = Objects.requireNonNull(ip, () -> {
			logger.fatal("IP cannot be null");
			return "IP cannot be null";
		});

		logger.debug("IP is " + ip);

		if (observer == null) {
			logger.warn("No observer, switching to default");
			api.hostByIp(ip).subscribe(DEFAULT_HOST_LOGGER);
		} else api.hostByIp(ip).subscribe(observer);
	}

	public static void searchWith(@NotNull String queries, DisposableObserver<HostReport> observer) {
		searchWith(queries, 1, observer);
	}

	public static synchronized void searchWith(@NotNull String queries, int page,
	                                           DisposableObserver<HostReport> observer) {

		checkCredits();

		queries = Objects.requireNonNull(queries, () -> {
			logger.fatal("Queries cannot be null");
			return "Queries cannot be null";
		});

		logger.debug("Queries is " + queries);

		if (observer == null) {
			logger.warn("No observer, switching to default");
			api.hostSearch(page, queries, "").subscribe(DEFAULT_HOSTREPORT_LOGGER);
		} else api.hostSearch(page, queries, "").subscribe(observer);
	}

	public static final DisposableLoggerObserver<HostReport> DEFAULT_HOSTREPORT_LOGGER =
			new DisposableLoggerObserver<>();

	public static final DisposableLoggerObserver<Host> DEFAULT_HOST_LOGGER = new DisposableLoggerObserver<>();

	private static class DisposableLoggerObserver<T> extends DisposableObserver<T> {

		private static final Logger logger = Logger.getLogger(DisposableLoggerObserver.class);

		@Override
		protected void onStart() {
			logger.debug("Start observe result.");
			super.onStart();
		}

		@Override
		public void onNext(T t) {
			logger.debug("Next is " + t.toString());
		}

		@Override
		public void onError(Throwable e) {
			logger.critical(e.getMessage());
		}

		@Override
		public void onComplete() {
			logger.debug("Observe complete.");
		}
	}

	public static class DisposableObserverAdapter<T> extends DisposableObserver<T> {

		private static final Logger logger = Logger.getLogger(DisposableObserverAdapter.class);

		private List<DisposableObserver<T>> list;

		public DisposableObserverAdapter() {
			this.list = new ArrayList<>();
		}

		@Contract("_ -> this")
		public DisposableObserverAdapter<T> add(DisposableObserver<T> observer) {
			list.add(observer);
			return this;
		}

		@Override
		public void onNext(T t) {
			if (list.isEmpty()) return;

			// We should stop executing immediately while meeting exception
			try {
				list.forEach(o -> o.onNext(t));
			} catch (Throwable e) {
				logger.warn("Exception met while observing");
				onError(e);
			}
		}

		@Override
		public void onError(Throwable e) {
			if (list.isEmpty()) return;
			StringBuilder builder = new StringBuilder();

			// Potential throw exception without catch so we need to collect
			// all exception message and rethrow with Runtime Exception
			for (DisposableObserver<T> observer : list) {
				try {
					observer.onError(e);
				} catch (Throwable err) {
					builder.append(err.getMessage()).append("\n");
				}
			}

			if (builder.length() > 0) throw new RuntimeException(builder.toString());

		}

		@Override
		public void onComplete() {
			if (list.isEmpty()) return;

			// This method should be error free or it will invoke onError
			list.forEach(Observer::onComplete);
		}
	}
}
