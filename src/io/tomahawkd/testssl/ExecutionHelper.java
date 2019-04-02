package io.tomahawkd.testssl;

import io.tomahawkd.common.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecutionHelper {

	public static final String TAG = "[ExecutionHelper]";

	private static final String testssl = "./testssl.sh/testssl.sh --jsonfile=";
	private static final String path = "./temp/testssl/";
	private static final String extension = ".txt";

	// Return file path
	public static String runTest(String host) throws Exception {
		if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

		var file = path + host + extension;

		return FileHelper.Cache.getIfValidOrDefault(file, f -> f, () -> {
			System.out.println(TAG + " Testing " + host);
			run(testssl + file + " " + host);
			return file;
		});
	}

	public static String run(String command)
			throws IOException, InterruptedException {

		System.out.println(TAG + " Running command " + command);

		Process pro = Runtime.getRuntime().exec(command);
		InputStream in = pro.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		int charNum;
		var sb = new StringBuilder();
		while ((charNum = reader.read()) != -1) {
			System.out.print((char) charNum);
			sb.append((char) charNum);
		}

		int status = -1;
		try {
			status = pro.waitFor();
		} catch (InterruptedException e) {
			throw new InterruptedException(TAG + " " + e.getMessage());
		}

		if (status != 0) throw new IllegalArgumentException(TAG + " Exit with exit code " + status);

		return sb.toString();
	}
}
