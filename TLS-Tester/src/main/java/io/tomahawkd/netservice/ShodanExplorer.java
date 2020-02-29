package io.tomahawkd.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.common.provider.ShodanTargetProvider;
import io.tomahawkd.testssl.data.parser.CommonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class ShodanExplorer {

	private static final Logger logger = Logger.getLogger(ShodanExplorer.class);

	private static final String path = FileHelper.TEMP + "/shodan/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create shodan directory");
		}
	}

	public static ShodanTargetProvider explore(String query, ShodanTargetProvider t)
			throws Exception {
		return explore(query, 1, t);
	}

	public static ShodanTargetProvider explore(String query, int count,
	                                           ShodanTargetProvider t) throws Exception {
		return explore(query, 1, count, t);
	}

	public static ShodanTargetProvider explore(String query, int start, int count,
	                                           ShodanTargetProvider t) throws Exception {

		String file = path + URLEncoder.encode(query, Charset.defaultCharset().toString()) + extension;
		logger.debug("IP file: " + file);

		t.setRunning();
		String data = FileHelper.Cache.getContentIfValidOrDefault(file, () -> "");

		// valid
		if (!data.isEmpty()) {
			t.addAll(CommonParser.parseHost(data));
			t.setFinish();
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
		return t;
	}
}
