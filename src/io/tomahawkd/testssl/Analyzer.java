package io.tomahawkd.testssl;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
import io.tomahawkd.testssl.data.Segment;
import io.tomahawkd.testssl.data.SegmentMap;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Analyzer {

	public static TargetSegmentMap parseFile(String path) throws IOException {
		String file = FileHelper.readFile(path);
		System.out.println("Parsing " + path);
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

	public static void analyze(SegmentMap target) {
		if (LeakyChannelAnalyzer.checkVulnerable(target, true)) {
			System.err.println(target.getIp() + " is leaky.");
		} else System.out.println(target.getIp() + " is not leaky.");

		if (TaintedChannelAnalyzer.checkVulnerable(target)) {
			System.err.println(target.getIp() + " is tainted.");
		} else System.out.println(target.getIp() + " is not tainted.");
	}
}
