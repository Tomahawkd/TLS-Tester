package io.tomahawkd.common.log;

import io.tomahawkd.common.FileHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFileOutputDelegate implements LogHandler.OutputDelegate {

	private static final String path = "./log/";
	private static final String extension = ".log";

	static {
		try {
			File file = new File(path);
			if (!file.exists() && !file.mkdir()) throw new IOException();
		} catch (IOException e) {
			throw new RuntimeException("Could not create log directory");
		}
	}


	private String file;

	public LogFileOutputDelegate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMMMddHHmmss");
		this.file = path + dateFormat.format(new Date(System.currentTimeMillis())) + extension;
	}

	@Override
	public void publish(String message) throws Exception {
		FileHelper.writeFile(file, message + "\n", false);
	}
}
