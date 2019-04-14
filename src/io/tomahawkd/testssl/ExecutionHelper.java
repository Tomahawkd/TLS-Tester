package io.tomahawkd.testssl;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecutionHelper {

	private static final Logger logger = Logger.getLogger(ExecutionHelper.class);

	private static final String testssl = "./testssl.sh/testssl.sh --jsonfile=";
	private static final String path = "./temp/testssl/";
	private static final String extension = ".txt";

	// Return file path
	public static String runTest(String host) throws Exception {

		logger.info("Running testssl on " + host);
		if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

		String file = path + host + extension;

		return FileHelper.Cache.getIfValidOrDefault(file, f -> {
			String fl = FileHelper.readFile(f);
			try {
				JSONArray arr = (JSONArray) new JSONObject("{\"list\": " + fl + "}").get("list");
				String finding = (String) ((JSONObject) arr.get(arr.length() - 1)).get("finding");

				return arr.length() > 1 &&
						finding.trim().equals("Scan interrupted") &&
						FileHelper.Cache.isTempFileNotExpired(f);
			} catch (JSONException e) {
				return false;
			}

		}, f -> f, () -> {
			run(testssl + file + " " + host);
			return file;
		});
	}

	public static String run(String command)
			throws IOException, InterruptedException {

		logger.info("Running command " + command);

		Process pro = Runtime.getRuntime().exec(command);
		InputStream in = pro.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		int charNum;
		StringBuilder sb = new StringBuilder();
		StringBuilder line = new StringBuilder();
		while ((charNum = reader.read()) != -1) {
			sb.append((char) charNum);
			line.append((char) charNum);
			if (charNum == '\n') {
				System.out.print(line);
				line = new StringBuilder();
			}
		}

		int status = -1;
		try {
			status = pro.waitFor();
		} catch (InterruptedException e) {
			logger.fatal(e.getMessage());
			throw new InterruptedException(e.getMessage());
		}

		if (status != 0) {
			logger.fatal("Exit with exit code " + status);
			throw new IllegalArgumentException("Exit with exit code " + status);
		}

		return sb.toString();
	}
}
