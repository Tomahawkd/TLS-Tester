package io.tomahawkd.tlstester.data;

import com.fooock.shodan.model.host.Host;
import io.tomahawkd.tlstester.data.testssl.SegmentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.tomahawkd.tlstester.data.TargetInfo.CERT_HASH_NULL;

public class DataHelper {

	@Nullable
	public static Host getHostInfo(TargetInfo info) {
		return (Host) info.getCollectedData().get(InternalDataNamespace.SHODAN);
	}

	public static String getCountryCode(TargetInfo info) {
		Host hostInfo = getHostInfo(info);
		if (hostInfo == null || hostInfo.getCountryCode() == null) return "null";
		else return hostInfo.getCountryCode();
	}

	@NotNull
	public static SegmentMap getTargetData(TargetInfo info) {
		SegmentMap map = (SegmentMap) info.getCollectedData().get(InternalDataNamespace.TESTSSL);
		if (map == null) throw new RuntimeException("Required data not found");
		return map;
	}

	public static boolean isHasSSL(TargetInfo info) {
		Boolean b = (Boolean) info.getCollectedData().get(InternalDataNamespace.HAS_SSL);
		return b == null ? false : b;
	}

	public static String getBrand(TargetInfo info) {
		String brand = (String) info.getCollectedData().get(InternalDataNamespace.IDENTIFIER);
		if (brand == null) return "Unknown";
		return brand;
	}

	public static String getCertHash(TargetInfo info) {
		try {
			return (String) getTargetData(info).get("cert_fingerprintSHA256").getResult();
		} catch (RuntimeException e) {
			return CERT_HASH_NULL;
		}
	}
}
