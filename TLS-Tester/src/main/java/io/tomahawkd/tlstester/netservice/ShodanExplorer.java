package io.tomahawkd.tlstester.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.data.testssl.parser.CommonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class ShodanExplorer {

	private static final Logger logger = LogManager.getLogger(ShodanExplorer.class);

	private static final String path = FileHelper.TEMP + "/shodan/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create shodan directory");
		}
	}

	public static void explore(String query, StorableObserver t)
			throws Exception {
		explore(query, 1, t);
	}

	public static void explore(String query, int count, StorableObserver t) throws Exception {
		explore(query, 1, count, t);
	}

	public static void explore(String query, int start, int count,
	                           StorableObserver t) throws Exception {

		String file = path +
				URLEncoder.encode(query, Charset.defaultCharset().toString()) + extension;
		logger.debug("IP file: " + file);

		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> "");

		// valid
		if (!data.isEmpty()) {
			t.addAll(CommonParser.parseHost(data));
		} else {
			// invalid
			for (int i = 0; i < count; i++) {

				DisposableObserver<HostReport> adaptor =
						new ShodanQueriesHelper.DisposableObserverAdapter<HostReport>()
								.add(t).add(ShodanQueriesHelper.DEFAULT_HOSTREPORT_LOGGER)
								.add(new CacheObserver(file));

				ShodanQueriesHelper.searchWith(query, i + start, adaptor);
			}
		}
	}
}
