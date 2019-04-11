package io.tomahawkd.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

public class FileHelper {

	public static final String TAG = "[READER]";

	public static final String TEMP = "./temp/";

	static {
		try {
			if (!isDirExist(TEMP)) createDir(TEMP);
		} catch (IOException e) {
			System.err.println(TAG + "Temp directory cannot be created.");
		}
	}

	public static String readFile(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) throw new FileNotFoundException(TAG + "File Not Found.");
		if (!file.canRead()) throw new FileSystemException(TAG + "File Cannot be read.");

		try (FileInputStream in = new FileInputStream(file)) {
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) {
				builder.append(((char) c));
			}
			return builder.toString();
		} catch (IOException e) {
			throw new IOException(TAG + e.getMessage());
		}
	}

	public static boolean isDirExist(String path) {
		File file = new File(path);
		return file.exists() && file.isDirectory();
	}

	public static void writeFile(String path, String data, boolean overwrite) throws IOException {
		File file = new File(path);
		if (file.exists() && overwrite)
			if (!file.delete()) throw new FileSystemException(TAG + "File cannot be deleted.");

		if (!file.exists()) {
			if (!file.createNewFile()) throw new FileSystemException(TAG + "File cannot be created.");
		} else throw new FileAlreadyExistsException(TAG + " File already exist.");

		try (FileOutputStream out = new FileOutputStream(file)) {
			out.write(data.getBytes(StandardCharsets.UTF_8));
			out.flush();
		} catch (IOException e) {
			throw new IOException(TAG + e.getMessage());
		}
	}

	public static void createDir(String path) throws IOException {
		File file = new File(path);
		if (file.exists()) throw new FileAlreadyExistsException(TAG + " Directory already exist.");
		if (!file.mkdir()) throw new FileSystemException(TAG + "Directory cannot be created.");
	}

	public static boolean isFileExist(String path) {
		File file = new File(path);
		return file.exists() && file.isFile();
	}

	public static void deleteFile(String path) throws IOException {
		if (!isFileExist(path)) return;
		File file = new File(path);
		if (!file.delete()) throw new FileSystemException(TAG + "File cannot be deleted.");
	}

	public static class Cache {

		public static String getContentIfValidOrDefault(String file, ThrowableSupplier<String> onInvalid)
				throws Exception {

			String content = getIfValidOrDefault(file, FileHelper::readFile, onInvalid);

			// this is for cache
			if (!isTempFileNotExpired(file)) writeFile(file, content, true);
			return content;
		}

		public static String getIfValidOrDefault(
				String file, ThrowableFunction<String, String> onValid,
				ThrowableSupplier<String> onInvalid) throws Exception {

			if(FileHelper.isFileExist(file)) {
				if (!isTempFileExpired(file)) return onValid.apply(file);
				else FileHelper.deleteFile(file);
			}

			return onInvalid.get();
		}


		public static boolean isTempFileExpired(String path) {

			// file must be exist
			assert isFileExist(path);

			File file = new File(path);
			return System.currentTimeMillis() - file.lastModified() > 1000*60*60*24*7;
		}
	}
}
