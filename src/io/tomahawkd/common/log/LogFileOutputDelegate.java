package io.tomahawkd.common.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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


	private Path filePath;

	public LogFileOutputDelegate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String file = path + dateFormat.format(new Date(System.currentTimeMillis())) + extension;
		this.filePath = Paths.get(file);

		File f = filePath.toFile();
		try {
			if (f.exists() && !f.delete()) throw new RuntimeException("Cannot delete existing file " + file);
			if (!f.createNewFile()) throw new RuntimeException("Cannot create file " + file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error when creating file " + file);
		}
	}

	@Override
	public void publish(String message) throws Exception {

		message += "\n";

		if (!filePath.toFile().exists())
			Files.write(filePath, message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
		else Files.write(filePath, message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
	}
}
