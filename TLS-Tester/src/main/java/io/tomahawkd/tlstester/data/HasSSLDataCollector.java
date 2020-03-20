package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.tlsattacker.ConnectionTester;

@SuppressWarnings("unused")
@InternalDataCollector(order = 1)
@DataCollectTag(tag = InternalNamespaces.Data.HAS_SSL, type = Boolean.class)
public class HasSSLDataCollector implements DataCollector {
	@Override
	public Object collect(TargetInfo host) {
		try {
			return new ConnectionTester(host.getHost()).execute().isServerHelloReceived();
		} catch (Exception e) {
			return false;
		}
	}
}
