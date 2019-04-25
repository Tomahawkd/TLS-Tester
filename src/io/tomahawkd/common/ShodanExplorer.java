package io.tomahawkd.common;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.testssl.data.parser.TargetObserver;

import java.net.URLEncoder;
import java.util.List;

public class ShodanExplorer {

	private static final Logger logger = Logger.getLogger(ShodanExplorer.class);

	private static final String path = "./temp/shodan/query/";
	private static final String extension = ".txt";

	public static List<String> explore(String query) throws Exception {

		@SuppressWarnings("deprecated")
		String file = path + URLEncoder.encode(query) + extension;
		logger.debug("IP file: " + file);

		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			TargetObserver observer = CommonParser.getTargetObserver();
			DisposableObserver<HostReport> adaptor =
					new ShodanQueriesHelper.DisposableObserverAdapter<HostReport>()
							.add(observer).add(ShodanQueriesHelper.DEFAULT_LOGGER);

			ShodanQueriesHelper.searchWith(query, adaptor);
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
}
