package io.tomahawkd.tlstester.provider.sources;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Source(name = InternalNamespaces.Sources.RUNTIME)
public class RuntimeSource implements TargetSource {

	private static final Logger logger = LogManager.getLogger(RuntimeSource.class);
	private List<InetSocketAddress> list = new ArrayList<>();

	public RuntimeSource() {
	}

	public RuntimeSource(String args) {
		logger.fatal("You cannot use runtime source from command line.");
		throw new RuntimeException("Runtime source cannot be used in this situation");
	}

	@Override
	public void acquire(TargetStorage storage) {
		storage.addAll(list);
	}

	public void addAll(Collection<String> data) {
		SourcesStreamHelper.addTo(list, data.stream());
	}
}
