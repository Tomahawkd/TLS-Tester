package io.tomahawkd.tlstester.provider.sources;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Source(name = InternalNamespaces.Sources.FILE)
public class FileSource extends AbstractTargetSource {

	private static final Logger logger = LogManager.getLogger(FileSource.class);

	private String file;

	public FileSource(String args) {
		super(args);
		if (FileHelper.isFileExist(args)) {
			logger.error("File {} not found", args);
			throw new ParameterException("File " + args + " not found");
		}
		this.file = args;
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
