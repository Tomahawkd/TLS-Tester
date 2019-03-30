package io.tomahawkd.common;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

public class FileHelper {

	public static final String TAG = "[READER]";

	public static String readFile(String path) throws IOException {
		var file = new File(path);
		if (!file.exists()) throw new FileNotFoundException(TAG + "File Not Found.");
		if (!file.canRead()) throw new FileSystemException(TAG + "File Cannot be read.");

		try (FileInputStream in = new FileInputStream(file)) {
			return new String(in.readAllBytes(), Charset.forName("utf8"));
		} catch (IOException e) {
			throw new IOException(TAG + e.getMessage());
		}
	}

	public static boolean isDirExist(String path) {
		var file = new File(path);
		return file.exists() && file.isDirectory();
	}

	public static void createDir(String path) throws IOException {
		var file = new File(path);
		if (file.exists()) throw new FileAlreadyExistsException(TAG + " Directory already exist.");
		if(!file.mkdir()) throw new FileSystemException(TAG + "Directory cannot be created.");
	}
}
