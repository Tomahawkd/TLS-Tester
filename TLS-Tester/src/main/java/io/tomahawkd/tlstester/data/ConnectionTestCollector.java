package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.tlsattacker.ConnectionTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.HAS_SSL, type = boolean.class, order = 4)
public class ConnectionTestCollector implements DataCollector {

	private static final Logger logger = LogManager.getLogger(ConnectionTestCollector.class);

	@Override
	public Object collect(TargetInfo host) {
		String protocol = (String)
				host.getCollectedData().get(InternalNamespaces.Data.STARTTLS);

		try {
			ConnectionTester tester = new ConnectionTester(host.getHost());
			if (protocol != null) {
				tester.setStarttlsProtocol(protocol);
			}

			return tester.execute().isServerHelloReceived();
		} catch (Exception e) {
			if (protocol != null) {
				logger.warn("Starttls protocol connection failed, fallback to default");
				return new ConnectionTester(host.getHost()).execute().isServerHelloReceived();
			}
			return false;
		}
	}
}
