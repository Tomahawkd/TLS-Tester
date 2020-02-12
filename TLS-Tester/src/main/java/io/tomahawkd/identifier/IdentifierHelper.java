package io.tomahawkd.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.common.ComponentsLoader;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.netservice.ShodanQueriesHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IdentifierHelper {

	private static List<CommonIdentifier> identifiers = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(IdentifierHelper.class);

	static {

		logger.info("Initializing Identifier");

		ComponentsLoader
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

	@Nullable
	public static Host getInfoFromIp(String ip) {
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
		try {
			return hostObserver.getResult().get(0);
		} catch (IndexOutOfBoundsException e) {
			logger.warn("Read timeout, return null");
			return null;
		}
	}

	@Nullable
	public static CommonIdentifier identifyHardware(Host host) {

		if (host == null) return null;
		logger.info("identifying IP " + host.getIpStr());

		for (CommonIdentifier identifier : identifiers) {
			if (identifier.identify(host)) {
				return identifier;
			}
		}
		return new UnknownIdentifier();
	}
}
