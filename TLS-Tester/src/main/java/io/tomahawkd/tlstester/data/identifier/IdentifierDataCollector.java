package io.tomahawkd.tlstester.data.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.annotations.DataCollectTag;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.data.DataCollector;
import io.tomahawkd.tlstester.data.InternalDataCollector;
import io.tomahawkd.tlstester.data.InternalDataNamespace;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.identifier.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@InternalDataCollector(order = 3)
@DataCollectTag(tag = InternalDataNamespace.IDENTIFIER, type = String.class)
public class IdentifierDataCollector implements DataCollector {

	private static List<Identifier> identifiers = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(IdentifierDataCollector.class);

	static {

		logger.debug("Initializing Identifier");

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
	public Identifier identifyHardware(Host host) {

		if (host == null) return new UnknownIdentifier();
		logger.debug("identifying IP " + host.getIpStr());

		for (Identifier identifier : identifiers) {
			if (identifier.identify(host)) {
				return identifier;
			}
		}
		return new UnknownIdentifier();
	}

	@Override
	public Object collect(TargetInfo host) {
		return identifyHardware(host.getHostInfo()).tag();
	}
}
