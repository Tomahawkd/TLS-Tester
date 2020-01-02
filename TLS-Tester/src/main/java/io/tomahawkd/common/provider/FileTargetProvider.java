package io.tomahawkd.common.provider;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class FileTargetProvider<T> extends AbstractTargetProvider<T> {

	public static final Logger logger = Logger.getLogger(FileTargetProvider.class);
	private TargetProvider<String> provider;
	private Converter<T> converter;

	@NotNull
	@Contract("_ -> new")
	public static FileTargetProvider<String> getDefault(String path) {
		return new FileTargetProvider<>(path, DEFAULT, CommonParser::parseHost);
	}

	public FileTargetProvider(String path, Converter<T> converter) {
		this(path, converter, CommonParser::parseHost);
	}

	public FileTargetProvider(String path, Converter<T> converter, Parser parser) {
		try {
			String data = FileHelper.readFile(path);
			provider = new ListTargetProvider<>(parser.parse(data));
			this.converter = Objects.requireNonNull(converter, "Converter cannot be null");
		} catch (Exception e) {
			this.setStatus(State.FINISHED);
			logger.critical("File operation has met exception: " + e.getMessage());

		}
	}

	@Override
	public State getStatus() {
		return provider.getStatus();
	}

	@Override
	public T getNextTarget() {
		return converter.convert(provider.getNextTarget());
	}

	@FunctionalInterface
	public interface Converter<T> {

		T convert(String data);
	}

	@FunctionalInterface
	public interface Parser {
		List<String> parse(String data);
	}

	public static final Converter<String> DEFAULT = s -> s;
}
