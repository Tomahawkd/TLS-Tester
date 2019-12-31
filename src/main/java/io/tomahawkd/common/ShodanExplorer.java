package io.tomahawkd.common;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.testssl.data.parser.TargetObserver;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class ShodanExplorer {

	private static final Logger logger = Logger.getLogger(ShodanExplorer.class);

	private static final String path = "./temp/shodan/query/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create shodan directory");
		}
	}

	public static List<String> explore(String query) throws Exception {
		return explore(query, 1);
	}

	public static List<String> explore(String query, int count) throws Exception {
		return explore(query, 1, count);
	}

	public static List<String> explore(String query, int start, int count) throws Exception {

		String file = path + URLEncoder.encode(query, Charset.defaultCharset().toString()) + extension;
		logger.debug("IP file: " + file);

		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> {
			TargetObserver observer = CommonParser.getTargetObserver();

			for (int i = 0; i < count; i++) {

				DisposableObserver<HostReport> adaptor =
						new ShodanQueriesHelper.DisposableObserverAdapter<HostReport>()
								.add(observer).add(ShodanQueriesHelper.DEFAULT_HOSTREPORT_LOGGER);

				ShodanQueriesHelper.searchWith(query, i + start, adaptor);
				while (!observer.isComplete()) {
					try {
						logger.info("Not complete, sleeping");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.warn("Got interrupted");
						break;
					}
				}
			}

			return observer.toString();
		});

		return CommonParser.parseHost(data);
	}
}
