package io.tomahawkd.testssl;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.exception.NoSSLConnectionException;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.CipherInfo;
import io.tomahawkd.testssl.data.parser.CommonParser;
import io.tomahawkd.tlsattacker.ConnectionTester;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

public class ExecutionHelper {

	private static final Logger logger = Logger.getLogger(ExecutionHelper.class);

	private static final String testssl = "./testssl.sh/testssl.sh --jsonfile=";
	private static final String path = "./temp/testssl/";
	private static final String extension = ".txt";

	// Return file path
	public static String runTest(String host) throws NoSSLConnectionException, Exception {

		logger.info("Running testssl on " + host);
		try {
			boolean isSSL = new ConnectionTester(host)
					.setNegotiateVersion(CipherInfo.SSLVersion.TLS1_2)
					.execute()
					.isServerHelloReceived();

			if (isSSL) {

				if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

				String file = path + host + extension;

				return FileHelper.Cache.getIfValidOrDefault(file, f -> {
					String fl = FileHelper.readFile(f);
					try {
						JSONArray arr = (JSONArray) new JSONObject("{\"list\": " + fl + "}").get("list");
						for (Object object : arr) {
							if (((String) ((JSONObject) object).get("severity")).trim().equals("FATAL") ||
									((String) ((JSONObject) object).get("finding")).trim().equals("Scan interrupted"))
								return false;
						}

						return arr.length() > 1 && FileHelper.Cache.isTempFileNotExpired(f);
					} catch (JSONException e) {
						return false;
					}

				}, f -> f, () -> {
					run(testssl + file + " " + host);
					return file;
				});

			} else {
				logger.warn("host " + host + " do not have ssl connection, skipping.");
				throw new NoSSLConnectionException();
			}
		} catch (TransportHandlerConnectException e) {
			if (e.getCause() instanceof SocketTimeoutException)
				logger.critical("Connecting to host " + host + " timed out, skipping.");
			else logger.critical(e.getMessage());
			throw new NoSSLConnectionException(e.getMessage());
		}
	}

	public static String run(String command)
			throws IOException, InterruptedException {

		logger.info("Running command " + command);

		Process pro = Runtime.getRuntime().exec(command);
		InputStream in = pro.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		int charNum;
		StringBuilder sb = new StringBuilder();
		while ((charNum = reader.read()) != -1) {
			sb.append((char) charNum);
		}

		int status = -1;
		try {
			status = pro.waitFor();
		} catch (InterruptedException e) {
			logger.fatal(e.getMessage());
			throw new InterruptedException(e.getMessage());
		}

		if (status != 0) {
			logger.critical("Exit with exit code " + status);
		}

		logger.info("\n" + sb.toString());
		return sb.toString();
	}
}
