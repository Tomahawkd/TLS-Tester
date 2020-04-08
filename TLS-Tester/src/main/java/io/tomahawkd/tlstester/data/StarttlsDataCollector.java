package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.InternalNamespaces;

@SuppressWarnings("unused")
@DataCollectTag(tag = InternalNamespaces.Data.STARTTLS, type = String.class, order = 1)
public class StarttlsDataCollector implements DataCollector {

	@Override
	public Object collect(TargetInfo host) {

		int port = host.getAddress().getPort();
		switch (port) {
			case 21:
				return InternalNamespaces.Protocol.FTP;

			case 993:
				return InternalNamespaces.Protocol.IMAP;

			case 995:
				return InternalNamespaces.Protocol.POP3;

			case 25:
				return InternalNamespaces.Protocol.SMTP;

			default:
				return null;

		}
	}
}
