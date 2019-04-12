package io.tomahawkd.testssl.data.parser;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonParser {

	public static final String TAG = "[CommonParser]";

	private static void errorReport(String type, String finding, String message) {
		System.err.println(TAG + "Exception during parsing " + type + " with value \"" +
				finding + "\"" + "with message " + "\"" + message + "\"");
	}

	private static void errorReport(String type, String finding) {
		System.err.println(TAG + "Exception during parsing " + type + " with value \"" + finding + "\"");
	}

	public static TargetSegmentMap parseFile(String path) throws IOException {
		String file = FileHelper.readFile(path);
		System.out.println(TAG + " Parsing " + path);
		JSONArray arr = (JSONArray) new JSONObject("{\"list\": " + file + "}").get("list");
		TargetSegmentMap map = new TargetSegmentMap();
		for (Object item : arr) {
			JSONObject object = (JSONObject) item;

			String id = (String) object.get("id");
			String ip = (String) object.get("ip");
			String port = (String) object.get("port");
			String severity = (String) object.get("severity");
			String finding = (String) object.get("finding");
			String cve = "";
			String cwe = "";
			try {
				cve = (String) object.get("cve");
				cwe = (String) object.get("cwe");
				map.add(new Segment(id, ip, port, severity, finding, cve + " " + cwe));
			} catch (JSONException e) {
				String exploit = cve + " " + cwe;
				if (exploit.isEmpty()) map.add(new Segment(id, ip, port, severity, finding));
				else map.add(new Segment(id, ip, port, severity, finding, exploit));
			}
		}

		return map;
	}

	public static String returnSelf(String finding) {
		return finding;
	}

	public static CipherInfo parseCipherInfo(String finding) {
		String[] findings = finding.split(CipherInfo.splitSign);
		return new CipherInfo(findings[0].split("cipherorder_")[1], parseList(findings[1]));
	}

	private static OfferedResult parseIsString(String finding, String target) {
		if (finding.equals(target)) {
			return new OfferedResult(true, null);
		} else if (finding.startsWith(target)) {
			String info = finding.split(target)[1].trim();
			return new OfferedResult(true, info);
		} else if (finding.equals("not " + target)) {
			return new OfferedResult(false, null);
		} else if (finding.startsWith("not " + target)) {
			String info = finding.split("not " + target)[1].trim();
			return new OfferedResult(false, info);
		} else {
			return new OfferedResult(false, "invalid");
		}
	}

	public static OfferedResult isSupported(String finding) {
		return parseIsString(finding, "supported");
	}

	public static OfferedResult isVulnerable(String finding) {
		return parseIsString(finding, "vulnerable");
	}

	public static OfferedResult isOffered(String finding) {
		return parseIsString(finding, "offered");
	}

	public static OfferedResult isTrue(String finding) {
		if (finding.equals("yes")) return new OfferedResult(true, null);
		else if (finding.startsWith("yes")) return new OfferedResult(true, finding.split("yes")[1]);
		else if (finding.equals("no")) return new OfferedResult(false, null);
		else if (finding.startsWith("no")) return new OfferedResult(false, finding.split("no")[1]);
		else return new OfferedResult(false, finding);
	}

	public static OfferedResult isOk(String finding) {
		return parseIsString(finding, "Ok");
	}

	public static boolean isPassed(String finding) {
		return finding.startsWith("passed");
	}

	public static int parseInt(String finding) {
		return CommonParser.parseInt(finding, 10);
	}

	static int parseInt(String finding, int radix) {
		try {
			return Integer.parseInt(finding, radix);
		} catch (NumberFormatException e) {
			errorReport("integer", finding);
			return -1;
		}
	}

	public static X509Certificate parseCert(String finding) {
		X509Certificate certificate = null;
		CertificateFactory cf;
		try {
			if (finding != null && !finding.trim().isEmpty()) {

				finding = finding.replace("-----BEGIN CERTIFICATE-----", "")
						.replace("-----END CERTIFICATE-----", "").trim();

				finding = "-----BEGIN CERTIFICATE-----\n" + finding + "-----END CERTIFICATE-----";

				cf = CertificateFactory.getInstance("X509");
				certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(finding.getBytes()));
			}
		} catch (CertificateException e) {
			errorReport("cert", finding, e.getMessage());
		}
		return certificate;
	}

	public static int parseTime(String finding) {
		if (finding.startsWith("expires"))
			return CommonParser.parseInt(finding.substring(finding.indexOf("(") + 1, finding.indexOf(")")));
		return CommonParser.parseInt(finding.split(" ")[0]);
	}

	public static CipherSuiteSet parseCipherSuite(String finding) {
		String[] data = finding.split(" ");
		List<String> sliced = new ArrayList<>();
		CipherSuiteSet cipherSuiteList = new CipherSuiteSet();
		for (String datum : data) {
			if (!datum.equals("")) sliced.add(datum);
		}
		try {
			String hex = sliced.get(0);
			String name = sliced.get(1);
			StringBuilder keyExchange = new StringBuilder();
			for (int i = 2; i < sliced.size() - 3; i++) {
				keyExchange.append(sliced.get(i)).append(" ");
			}
			String enc = sliced.get(sliced.size() - 3);
			String bits = sliced.get(sliced.size() - 2);
			String rfc = sliced.get(sliced.size() - 1);
			cipherSuiteList.add(new CipherSuite(hex, name, keyExchange.toString().trim(), enc, bits, rfc));
		} catch (IndexOutOfBoundsException e) {
			errorReport("string", finding);
		}

		return cipherSuiteList;
	}

	public static class NameList {

		private List<String> list;

		public NameList(List<String> list) {
			this.list = list;
		}

		public List<String> getList() {
			return list;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			list.forEach(e -> builder.append(e).append(" "));
			return builder.toString().trim();
		}
	}

	public static NameList parseList(String finding) {
		return new NameList(Arrays.asList(finding.split(" ")));
	}

	public static CountableResult parseCount(String finding) {
		String[] list = finding.split(" ", 4);
		if (list.length == 3) return new CountableResult(CommonParser.parseInt(list[0]), list[2], null);
		else return new CountableResult(CommonParser.parseInt(list[0]), list[1], list[2]);
	}

	public static PercentageResult parsePercentage(String finding) {
		String[] list = finding.split(" ", 4);
		String[] percentage = list[0].split("/");
		if (list.length == 3)
			return new PercentageResult(CommonParser.parseInt(percentage[0]),
					CommonParser.parseInt(percentage[1]), list[2], null);
		else return new PercentageResult(CommonParser.parseInt(percentage[0]),
				CommonParser.parseInt(percentage[1]), list[2], list[3]);
	}

	public static List<String> parseHost(String hosts) {
		return new ArrayList<>(Arrays.asList(hosts.split("\n")));
	}

	public static IpObserver getIpParser() {
		return new IpObserver();
	}
}