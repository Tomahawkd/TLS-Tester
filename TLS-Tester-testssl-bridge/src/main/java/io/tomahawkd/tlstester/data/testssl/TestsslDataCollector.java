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
import io.tomahawkd.tlstester.data.testssl.exception.FatalTagFoundException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
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

		if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

		String file = path + host.getHost() + ".txt";

		Object hasConn = host.getCollectedData()
				.get(InternalNamespaces.Data.HAS_SSL);

		if (hasConn == null || !(boolean) hasConn) {
			throw new RuntimeException("Host does not have a valid connection");
		}

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
					// according to https://stackoverflow.com
					// /questions/38393979/defunct-processes-
					// when-java-start-terminal-execution
					// redirect to null
					run(ArgConfigurator.INSTANCE
									.getByType(TestsslArgDelegate.class)
									.getTestsslPath() + "/testssl.sh",
							"-s", "-p", "-S", "-P", "-h", "-U",
							"--warnings=batch",
							"--connect-timeout=10",
							"--openssl-timeout=10",
							"--jsonfile=" + file, host.getHost());
					return file;
				});

		try {
			String result = FileHelper.readFile(filename);
			List<Segment> r = new GsonBuilder().create()
					.fromJson(result, new TypeToken<List<Segment>>() {
					}.getType());
			SegmentMap map = new SegmentMap();
			r.forEach(map::add);
			map.setComplete();
			return map;
		} catch (FileNotFoundException e) {
			// testssl met a major error
			logger.error("testssl error occurs when testing host {}, skipping.",
					host.getHost(), e);
			throw new RuntimeException("Testssl error occurs.");
		} catch (FatalTagFoundException e) {
			// testssl would report a file with fatal tag when connection is failed
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void run(String... command) throws IOException, InterruptedException {
		logger.info("Running command {}", () -> Arrays.toString(command));

		// according to https://stackoverflow.com/
		// questions/60975336/java-no-such-file-or-directory-when-
		// redirecting-command-line-program-output-to
		// it is recommend to use redirect output rather than > /dev/null
		ProcessBuilder b = new ProcessBuilder(command);
		b.redirectOutput(Paths.get("/dev/null").toFile()).redirectErrorStream(true);
		Process pro = b.start();
		try {
			if (!pro.waitFor(5, TimeUnit.MINUTES)) {
				pro.destroyForcibly();
				int code = pro.waitFor();

				String msg = "Time limit exceeded, force terminated with code " + code;
				logger.error(msg);
				throw new IOException(msg);
			} else {
				int exit = pro.exitValue();
				if (exit != 0) logger.warn("Exit code is " + exit);
				logger.debug("Testssl exit successfully");
			}
		} catch (InterruptedException e) {
			throw logger.throwing(Level.FATAL, e);
		}
	}
}
