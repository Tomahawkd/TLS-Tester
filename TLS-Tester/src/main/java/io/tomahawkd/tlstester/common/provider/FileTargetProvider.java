package io.tomahawkd.tlstester.common.provider;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileTargetProvider<T> extends CommonTargetProvider<T> {

	public static final Logger logger = LogManager.getLogger(FileTargetProvider.class);

	public static FileTargetProvider<String> getDefault(String path) {
		return new FileTargetProvider<>(path, s -> s);
	}

	public FileTargetProvider(String path, Converter<T> converter) {
		try {
			addAll(Files.readAllLines(Paths.get(path)).stream()
					.filter(l -> !l.trim().startsWith("#"))
					.map(converter::convert)
					.collect(Collectors.toList()));
		} catch (Exception e) {
			this.setStatus(State.FINISHED);
			logger.error("File operation has met exception: " + e.getMessage());
		}

		setStatus(State.FINISHING);
	}

	@FunctionalInterface
	public interface Converter<T> {
		T convert(String data);
	}
}
