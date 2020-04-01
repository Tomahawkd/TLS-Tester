package io.tomahawkd.tlstester.data.testssl;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.config.ArgConfigurator;
import io.tomahawkd.tlstester.config.TestsslArgDelegate;
import io.tomahawkd.tlstester.data.DataCollectTag;
import io.tomahawkd.tlstester.data.DataCollector;
import io.tomahawkd.tlstester.data.TargetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.TESTSSL, type = SegmentMap.class, order = 4)
public class TestsslDataCollector implements DataCollector {

	private static final Logger logger = LogManager.getLogger(TestsslDataCollector.class);
	private static final String path = "./temp/testssl/";

	@Override
	public Object collect(TargetInfo host) throws Exception {

		logger.info("Running testssl on " + host.getHost());

		Boolean b = (Boolean) host.getCollectedData().get(InternalNamespaces.Data.HAS_SSL);
		if (b == null ? false : b) {

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
						// seems openssl-timeout could report a error in newer version
						// while there is no program named timeout
						run(ArgConfigurator.INSTANCE
								.getByType(TestsslArgDelegate.class)
								.getTestsslPath() + "/testssl.sh",
								"-s", "-p", "-S", "-P", "-h", "-U",
								"--warnings=off", "--jsonfile=" + file, host.getHost());
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
			throw new RuntimeException(
					"host " + host.getHost() + " do not have ssl connection, skipping.");
		}
	}

	private void run(String... command) throws IOException, InterruptedException {
		logger.info("Running command " + Arrays.toString(command));

		// according to https://stackoverflow.com
		// /questions/38393979/defunct-processes-
		// when-java-start-terminal-execution
		ProcessBuilder b = new ProcessBuilder(command);
		b.redirectOutput(Paths.get("/dev/null").toFile()).redirectErrorStream(true);
		Process pro = b.start();

		try {
			// the input stream already read all bytes
			if (!pro.waitFor(5, TimeUnit.MINUTES)) {
				pro.destroy();
				logger.error("Time limit exceeded, force terminated");
				throw new IOException("Time limit exceeded, force terminated");
			} else {
				int exit = pro.exitValue();
				if (exit != 0) logger.warn("Exit code is " + exit);
				logger.debug("Testssl exit successfully");
			}
		} catch (InterruptedException e) {
			logger.fatal(e);
			throw new InterruptedException(e.getMessage());
		}
	}
}
