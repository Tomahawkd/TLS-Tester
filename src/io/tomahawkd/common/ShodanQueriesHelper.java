package io.tomahawkd.common;

import com.fooock.shodan.ShodanRestApi;
import com.fooock.shodan.model.host.HostReport;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShodanQueriesHelper {

	public static final String TAG = "[ShodanQueriesHelper]";

	private static final String path = "./temp/shodan";
	private static final String extension = ".txt";

	private static ShodanRestApi api;


	static {
		try {
			api = new ShodanRestApi(FileHelper.readFile("./temp/api_key"));
			api.info().subscribe(e -> {
				int credits = e.getQueryCredits();
				if (credits <= 0) throw new IllegalArgumentException(TAG + " No more credits(" + credits + ")");
			}).dispose();
		} catch (IOException e) {
			System.err.println(TAG + " Error on loading api file");
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}
	}

	public static List<String> searchIpWithSerial(String serial) throws Exception {

		var file = path + serial + extension;

		var data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			var observer = CommonParser.getIpParser();
			var adaptor = new DisposableObserverAdapter<HostReport>().add(observer).add(DEFAULT_LOGGER);

			searchWithSerial(serial, adaptor);
			while (!observer.isComplete()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					break;
				}
			}

			StringBuilder builder = new StringBuilder();
			observer.getIps().forEach(e->builder.append(e).append("\n"));
			return builder.toString();
		});

		return CommonParser.parseHost(data);
	}

	public static void searchWithSerial(String serial, DisposableObserver<HostReport> observer) {
		searchWith("ssl.cert.serial:" + serial, observer);
	}

	public static void searchWith(@NotNull String queries, DisposableObserver<HostReport> observer) {

		queries = Objects.requireNonNull(queries, TAG + " Queries cannot be null.");
		api.hostSearch(queries).subscribe(Objects.requireNonNullElse(observer, DEFAULT_LOGGER));
	}

	public static final DisposableLoggerObserver<HostReport> DEFAULT_LOGGER = new DisposableLoggerObserver<>();

	private static class DisposableLoggerObserver<T> extends DisposableObserver<T> {

		public static final String TAG = "[DisposableLoggerObserver]";

		@Override
		protected void onStart() {
			System.out.println(TAG + " Start observe result.");
			super.onStart();
		}

		@Override
		public void onNext(T t) {
			System.out.println(TAG + " " + t.toString());
		}

		@Override
		public void onError(Throwable e) {
			System.err.println(TAG + " " + e.getMessage());
		}

		@Override
		public void onComplete() {
			System.out.println(TAG + " Observe complete.");
		}
	}

	public static class DisposableObserverAdapter<T> extends DisposableObserver<T> {

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
