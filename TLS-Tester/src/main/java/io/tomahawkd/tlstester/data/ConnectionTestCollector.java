package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.tlsattacker.ConnectionTester;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.HAS_SSL, type = boolean.class, order = 3)
public class ConnectionTestCollector implements DataCollector {

	@Override
	public Object collect(TargetInfo host) {
		try {
			return new ConnectionTester(host.getHost()).execute().isServerHelloReceived();
		} catch (Exception e) {
			return false;
		}
	}
}
