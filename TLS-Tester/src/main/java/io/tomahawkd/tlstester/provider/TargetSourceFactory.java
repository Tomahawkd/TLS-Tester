package io.tomahawkd.tlstester.provider;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.provider.sources.Source;
import io.tomahawkd.tlstester.provider.sources.TargetSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum TargetSourceFactory {

	INSTANCE;

	private Map<String, Class<? extends TargetSource>> sources;

	private Logger logger = LogManager.getLogger(TargetSourceFactory.class);

	TargetSourceFactory() {
		sources = new HashMap<>();
		Set<Class<? extends TargetSource>> pd =
				ComponentsLoader.INSTANCE.loadClasses(TargetSource.class);

		for (Class<? extends TargetSource> c : pd) {
			if (Modifier.isAbstract(c.getModifiers())) continue;
			logger.debug("Loading source metadata " + c.getName());

			Source s = c.getAnnotation(Source.class);
			if (s != null) {
				if (sources.containsKey(s.name())) {
					logger.fatal("Source name overlapping.");
					throw new IllegalArgumentException("Source name overlapping.");
				} else {
					sources.put(s.name(), c);
				}
			}
		}
	}

	public TargetSource build(String args) {
		logger.debug("Start parsing " + args);
		String[] l = args.split("::", 2);
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
	}
}
