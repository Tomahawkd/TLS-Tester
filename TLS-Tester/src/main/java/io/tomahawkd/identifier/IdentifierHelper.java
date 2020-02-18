package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.ComponentsLoader;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.netservice.ShodanQueriesHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IdentifierHelper {

	private static List<CommonIdentifier> identifiers = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(IdentifierHelper.class);

	static {

		logger.info("Initializing Identifier");

		ComponentsLoader.INSTANCE
				.loadClasses(CommonIdentifier.class, IdentifierHelper.class.getPackage())
				.forEach(clazz -> {
					try {
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
	public static CommonIdentifier identifyHardware(Host host) {

		if (host == null) return new UnknownIdentifier();
		logger.debug("identifying IP " + host.getIpStr());

		for (CommonIdentifier identifier : identifiers) {
			if (identifier.identify(host)) {
				return identifier;
			}
		}
		return new UnknownIdentifier();
	}
}
