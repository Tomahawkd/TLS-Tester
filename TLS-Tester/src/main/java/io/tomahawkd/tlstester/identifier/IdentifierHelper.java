package io.tomahawkd.tlstester.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.log.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class IdentifierHelper {

	private static List<Identifier> identifiers = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(IdentifierHelper.class);

	static {

		logger.info("Initializing Identifier");

		ComponentsLoader.INSTANCE
				.loadClasses(Identifier.class).forEach(clazz -> {
			try {

				if (Modifier.isAbstract(clazz.getModifiers())) return;
				if (UnknownIdentifier.class.equals(clazz)) return;
				identifiers.add(clazz.newInstance());

				logger.debug("Adding Identifier " + clazz.getName());
			} catch (InstantiationException |
					IllegalAccessException |
					ClassCastException e) {
				logger.critical("Exception during initialize identifier: " + clazz.getName());
				logger.critical(e.getMessage());
			}
		});
	}

	@NotNull
	@Contract("null -> new")
	public static Identifier identifyHardware(Host host) {

		if (host == null) return new UnknownIdentifier();
		logger.debug("identifying IP " + host.getIpStr());

		for (Identifier identifier : identifiers) {
			if (identifier.identify(host)) {
				return identifier;
			}
		}
		return new UnknownIdentifier();
	}
}
