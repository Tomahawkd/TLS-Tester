package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.ShodanQueriesHelper;
import io.tomahawkd.common.log.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IdentifierHelper {

	private static List<CommonIdentifier> identifiers = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(IdentifierHelper.class);

	static {

		List<ClassLoader> classLoadersList = new ArrayList<>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(true),
						new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(
						new FilterBuilder().include(
								FilterBuilder.prefix(IdentifierHelper.class.getPackage().getName()))));
		Set<Class<? extends CommonIdentifier>> classes = reflections.getSubTypesOf(CommonIdentifier.class);

		// Ignore unknown identifier class
		classes.remove(UnknownIdentifier.class);
		classes.forEach(clazz -> {
			Class[] param = {};
			try {
				identifiers.add(clazz.getConstructor(param).newInstance());
			} catch (InstantiationException |
					NoSuchMethodException |
					InvocationTargetException |
					IllegalAccessException |
					ClassCastException e) {
				logger.critical("Exception during initialize identifier");
				logger.critical(e.getMessage());
			}
		});
	}

	public static CommonIdentifier identifyHardware(String ip) {

		logger.info("identifying IP " + ip);

		HostObserver<Host> hostObserver = new HostObserver<>();
		ShodanQueriesHelper.searchWithIp(ip, hostObserver);

		while (!hostObserver.isComplete()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}

		// this should only have 1 result
		Host host = hostObserver.getResult().get(0);
		for (CommonIdentifier identifier : identifiers) {
			if (identifier.identify(host)) {
				return identifier;
			}
		}

		return new UnknownIdentifier();
	}
}
