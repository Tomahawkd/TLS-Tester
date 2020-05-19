package io.tomahawkd.tlstester.data.identifier;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.data.DataCollectTag;
import io.tomahawkd.tlstester.data.DataCollector;
import io.tomahawkd.tlstester.data.DataHelper;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.extensions.ExtensionHandler;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;
import io.tomahawkd.tlstester.identifier.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.IDENTIFIER, type = String.class, order = 3)
public class IdentifierDataCollector implements DataCollector, ExtensionHandler {

	private static final List<Identifier> identifiers = new ArrayList<>();

	private static final Logger logger = LogManager.getLogger(IdentifierDataCollector.class);

	@Override
	public boolean canAccepted(Class<? extends ExtensionPoint> clazz) {
		return Identifier.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean accept(ExtensionPoint extension) {
		if (!(extension instanceof UnknownIdentifier)) {
			logger.debug("Adding Identifier " + extension.getClass().getName());
			identifiers.add((Identifier) extension);
		}

		return true;
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
