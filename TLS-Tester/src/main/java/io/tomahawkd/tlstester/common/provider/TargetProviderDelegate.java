package io.tomahawkd.tlstester.common.provider;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.provider.delegate.ProviderDelegateParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TargetProviderDelegate {

	private static final Logger logger = LogManager.getLogger(TargetProviderDelegate.class);

	public static TargetProvider<String> convert(String s) {
		List<ProviderDelegateParser> parsers = new ArrayList<>();

		Set<Class<? extends ProviderDelegateParser>> pd =
				ComponentsLoader.INSTANCE.loadClasses(ProviderDelegateParser.class,
						ProviderDelegateParser.class.getPackage());

		for (Class<? extends ProviderDelegateParser> c : pd) {
			try {
				logger.debug("Adding parser " + c.getName());
				parsers.add(c.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Parser " + c.getName() + " instantiation failed");
			}
		}

		logger.debug("Start parsing " + s);
		String[] l = s.split("::", 2);
		if (l.length != 2) throw new ParameterException("Malformed format");

		for (ProviderDelegateParser p : parsers) {
			try {
				if (p.identify(l[0])) {
					logger.debug("Using parser " + p.getClass());
					return p.parse(l[1]);
				}
			} catch (ParameterException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new ParameterException("Target type not found");
	}
}
