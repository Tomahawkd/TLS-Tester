package io.tomahawkd.detect;

import io.tomahawkd.identifier.CommonIdentifier;
import io.tomahawkd.identifier.IdentifierHelper;

public class StatisticRecoder {

	public static void addRecord(String ip, boolean isSSL, boolean leaky, boolean tainted, boolean partial) {

		CommonIdentifier identifier = IdentifierHelper.identifyHardware(ip);

		if (isSSL) {


		} else {


		}
	}
}
