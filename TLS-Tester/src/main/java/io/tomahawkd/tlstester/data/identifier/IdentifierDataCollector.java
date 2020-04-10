package io.tomahawkd.tlstester.data.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.data.DataCollectTag;
import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.data.*;
import io.tomahawkd.tlstester.identifier.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.IDENTIFIER, type = String.class, order = 3)
public class IdentifierDataCollector implements DataCollector {

	private static List<Identifier> identifiers = new ArrayList<>();

	private static final Logger logger = LogManager.getLogger(IdentifierDataCollector.class);

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
				logger.error("Exception during initialize identifier: " + clazz.getName(), e);
			}
		});
	}

	@NotNull
	@Contract("null -> new")
	public Identifier identifyHardware(Host host) {

		if (host == null) return new UnknownIdentifier("Null host data");
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
		return identifyHardware(DataHelper.getHostInfo(host)).tag();
	}
}
