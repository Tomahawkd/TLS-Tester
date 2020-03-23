package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileSource implements TargetSource {

	private static final Logger logger = LogManager.getLogger(FileSource.class);

	private String file;

	public FileSource(String filepath) throws FileNotFoundException {
		if (FileHelper.isFileExist(filepath)) {
			logger.error("File {} not found", filepath);
			throw new FileNotFoundException("File " + filepath + " not found");
		}
		this.file = filepath;
	}

	@Override
	public void acquire(TargetStorage storage) {
		try {
			storage.addAll(Files.readAllLines(Paths.get(file)).stream()
					.filter(l -> !l.trim().startsWith("#"))
					.collect(Collectors.toList()));
		} catch (IOException e) {
			logger.error("Cannot load file", e);
		}
	}
}
