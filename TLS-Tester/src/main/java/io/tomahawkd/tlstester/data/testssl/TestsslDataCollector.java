package io.tomahawkd.tlstester.data.testssl;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.ScanningArgDelegate;
import io.tomahawkd.tlstester.data.*;
import io.tomahawkd.tlstester.exception.NoSSLConnectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@InternalDataCollector(order = 4)
@DataCollectTag(tag = InternalNamespaces.Data.TESTSSL, type = SegmentMap.class)
public class TestsslDataCollector implements DataCollector {

	private static final Logger logger = LogManager.getLogger(TestsslDataCollector.class);
	private static final String path = "./temp/testssl/";

	@Override
	public Object collect(TargetInfo host) throws Exception {

		logger.info("Running testssl on " + host.getHost());
		try {
			if (DataHelper.isHasSSL(host)) {

				if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

				String file = path + host.getHost() + ".txt";

				String filename = FileHelper.Cache.getIfValidOrDefault(file, f -> {
							String fl = FileHelper.readFile(f);
							try {
								JSONArray arr =
										(JSONArray) new JSONObject("{\"list\": " + fl + "}")
												.get("list");
								for (Object object : arr) {
									if (((String) ((JSONObject) object)
											.get("severity")).trim().equals("FATAL") ||
											((String) ((JSONObject) object)
													.get("finding"))
													.trim().equals("Scan interrupted"))
										return false;
								}

								return arr.length() > 1 &&
										FileHelper.Cache.isTempFileNotExpired(f);
							} catch (JSONException e) {
								return false;
							}

						}, // isValid
						f -> f, // valid
						() -> { // invalid
							run(
									ArgConfigurator.INSTANCE
											.getByType(ScanningArgDelegate.class).getTestsslPath()
											+ "/testssl.sh -s -p -S -P -h -U " +
											"--warnings=off --openssl-timeout=10 " +
											"--jsonfile=" + file + " " + host.getHost());
							return file;
						});


				String result = FileHelper.readFile(filename);
				List<Segment> r = new GsonBuilder().create()
						.fromJson(result, new TypeToken<List<Segment>>() {
						}.getType());
				SegmentMap map = new SegmentMap();
				r.forEach(map::add);
				return map;

			} else {
				logger.warn("host " + host.getHost() + " do not have ssl connection, skipping.");
				throw new NoSSLConnectionException();
			}
		} catch (TransportHandlerConnectException e) {
			if (e.getCause() instanceof SocketTimeoutException)
				logger.warn("Connecting to host " + host.getHost() + " timed out, skipping.");
			else logger.warn(e.getMessage());
			throw new NoSSLConnectionException(e.getMessage());
		}
	}

	private void run(String command)
			throws IOException, InterruptedException {

		logger.info("Running command " + command);

		Process pro = Runtime.getRuntime().exec(command);
		try {
			if (!pro.waitFor(5, TimeUnit.MINUTES)) {
				pro.destroy();
				logger.error("Time limit exceeded, force terminated");
				throw new IOException("Time limit exceeded, force terminated");
			} else {
				int exit = pro.exitValue();
				if (exit != 0) logger.warn("Exit code is " + exit);
			}
		} catch (InterruptedException e) {
			logger.fatal(e);
			throw new InterruptedException(e.getMessage());
		}
	}
}
