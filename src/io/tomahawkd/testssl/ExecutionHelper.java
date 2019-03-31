package io.tomahawkd.testssl;

import io.tomahawkd.common.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecutionHelper {

	public static final String TAG = "[ExecutionHelper]";

	private static final String testssl = "/Users/ghost/Desktop/SSL/testssl.sh/testssl.sh --jsonfile=";
	private static final String path = "./temp/";
	private static final String extension = ".txt";

	public static String runTest(String host) throws IOException, InterruptedException {
		if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);

		var file = path + host + extension;
		if(FileHelper.isFileExist(file)) FileHelper.deleteFile(file);

		System.out.println(TAG + " Testing " + host);
		run(testssl + file + " " + host);
		return file;
	}

	public static String run(String command)
			throws IOException, InterruptedException {

		Process pro = Runtime.getRuntime().exec(command);
		InputStream in = pro.getInputStream();
		var res = new String(in.readAllBytes());

		int status = -1;
		try {
			status = pro.waitFor();
		} catch (InterruptedException e) {
			throw new InterruptedException(TAG + " " + e.getMessage());
		}

		if (status != 0) throw new IllegalArgumentException(TAG + " Exit with exit code " + status);

		return res;
	}
}
