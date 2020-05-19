package io.tomahawkd.tlstester.provider;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;
import io.tomahawkd.tlstester.extensions.ParameterizedExtensionHandler;
import io.tomahawkd.tlstester.extensions.ParameterizedExtensionPoint;
import io.tomahawkd.tlstester.provider.sources.Source;
import io.tomahawkd.tlstester.provider.sources.TargetSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TargetSourceFactory implements ParameterizedExtensionHandler {

	private final Map<String, Class<? extends TargetSource>> sources;

	private static final Logger logger = LogManager.getLogger(TargetSourceFactory.class);

	public TargetSourceFactory() {
		sources = new HashMap<>();
	}

	@Override
	public boolean canAccepted(Class<? extends ExtensionPoint> clazz) {
		return TargetSource.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean accept(ExtensionPoint extension) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean accept(Class<? extends ParameterizedExtensionPoint> extension) {
		Source s = extension.getAnnotation(Source.class);
		if (s == null) {
			logger.error("Target Source class {} has no annotation Source.", extension);
			return false;
		}

		logger.debug("Loading source metadata " + extension.getName());

		if (sources.containsKey(s.name())) {
			logger.fatal("Source name overlapping.");
			throw new IllegalArgumentException("Source name overlapping.");
		} else {
			logger.debug("Adding Source {}", extension);
			sources.put(s.name(), (Class<? extends TargetSource>) extension);
		}
		return true;
	}

	public List<TargetSource> build(List<String> args) {
		return args.stream()
				.map(arg -> {
					logger.debug("Start parsing " + arg);
					String[] l = arg.split("::", 2);
					if (l.length != 2) throw new ParameterException("Malformed format");

					Class<? extends TargetSource> c = sources.get(l[0]);
					if (c == null) throw new ParameterException("Target type not found");

					logger.debug("Using source {}", c.getName());
					try {
						return c.getConstructor(String.class).newInstance(l[1]);
					} catch (InstantiationException |
							InvocationTargetException |
							NoSuchMethodException |
							IllegalAccessException e) {
						logger.error("Class {} cannot be instantiated", c.getName(), e);
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());

	}
}
