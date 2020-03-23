package io.tomahawkd.tlstester.provider;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.provider.sources.TargetSource;
import io.tomahawkd.tlstester.provider.sources.TargetSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum TargetSourceFactoryProducer {

	INSTANCE;

	private List<TargetSourceFactory> factories;

	private Logger logger = LogManager.getLogger(TargetSourceFactoryProducer.class);

	TargetSourceFactoryProducer() {
		factories = new ArrayList<>();
		Set<Class<? extends TargetSourceFactory>> pd =
				ComponentsLoader.INSTANCE.loadClasses(TargetSourceFactory.class);

		for (Class<? extends TargetSourceFactory> c : pd) {
			try {
				logger.debug("Adding factory " + c.getName());
				factories.add(c.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("Factory {} instantiation failed", c.getName(), e);
				throw new RuntimeException("Factory " + c.getName() + " instantiation failed", e);
			}
		}
	}

	public TargetSource parse(String s) {
		logger.debug("Start parsing " + s);
		String[] l = s.split("::", 2);
		if (l.length != 2) throw new ParameterException("Malformed format");

		for (TargetSourceFactory p : factories) {
			try {
				if (p.identify(l[0])) {
					logger.debug("Using parser " + p.getClass());
					return p.build(l[1]);
				}
			} catch (ParameterException e) {
				throw e;
			} catch (Exception e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		}

		throw new ParameterException("Target type not found");
	}

}
