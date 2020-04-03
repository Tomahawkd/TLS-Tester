package io.tomahawkd.tlstester.data.testssl;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.tomahawkd.tlstester.data.testssl.exception.FatalTagFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tomahawkd
 */
@JsonAdapter(Segment.SegmentTypeAdapter.class)
public class Segment {

	private static final Logger logger = LogManager.getLogger(Segment.class);

	private String id;
	private String domain;
	private InetAddress ip;
	private int port;
	private Level severity;
	private String finding;
	private List<String> exploit;
	private Tag<?> tag;
	private Object result;

	public Segment(String id, String fqdn_ip, String port, String severity, String finding) {
		this(id, fqdn_ip, port, severity, finding, null);
	}

	public Segment(String id, String fqdn_ip, String port, String severity, String finding, String exploitNo) {
		try {
			this.id = id;
			this.tag = Tag.getTag(id);
			if (this.tag.getType() == SectionType.CIPHER_ORDER) {
				this.result = tag.parseCipher(id, finding);
			} else {
				this.result = tag.parseData(finding);
			}
			logger.debug("Parsed result [" + result + "]");

			String[] dnip = fqdn_ip.split("/");
			this.domain = dnip[0];
			this.ip = InetAddress.getByName(dnip[1]);
			this.port = Integer.parseInt(port);
			this.severity = Level.getByName(severity);
			this.finding = finding;
		} catch (UnknownHostException | NumberFormatException e) {
			logger.fatal(e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}
		this.exploit = exploitNo == null ? null : Arrays.asList(exploitNo.split(" "));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Segment &&
				this.id.equals(((Segment) obj).id) &&
				this.domain.equals(((Segment) obj).domain) &&
				this.ip.equals(((Segment) obj).ip) &&
				this.port == ((Segment) obj).port &&
				this.tag.equals(((Segment) obj).tag);
	}

	@SuppressWarnings("Unchecked")
	public void merge(Segment other) {

		logger.debug("Merging " + this + " & " + other);

		if (this.equals(other)) {
			this.severity = this.severity.getLevel() >= other.severity.getLevel() ? this.severity : other.severity;
			this.finding = this.finding + "\n" + other.finding;
			logger.debug("Finding merged");
		} else {
			logger.fatal("Not compatible to merge");
			throw new IllegalArgumentException("Not compatible to merge.");
		}
	}

	public String getId() {
		return id;
	}

	public String getDomain() {
		return domain;
	}

	public String getIp() {
		return ip.getHostAddress();
	}

	public InetSocketAddress getTarget() {
		return new InetSocketAddress(ip, port);
	}

	public int getPort() {
		return port;
	}

	public Level getSeverity() {
		return severity;
	}

	public String getFinding() {
		return finding;
	}

	public Tag<?> getTag() {
		return tag;
	}

	public Object getResult() {
		return result;
	}

	public String getExploit() {
		if (exploit == null) return "null";
		StringBuilder builder = new StringBuilder();
		exploit.forEach(e -> builder.append(e).append(" "));
		return builder.toString();
	}

	@Override
	public String toString() {
		return "{ " +
				"description: " + tag.getDescription() +
				", severity: " + getSeverity() +
				", result: " + getResult() +
				", exploit No: " + getExploit() + " }";
	}

	static class SegmentTypeAdapter extends TypeAdapter<Segment> {

		private static final Logger logger = LogManager.getLogger(SegmentTypeAdapter.class);

		@Override
		public void write(JsonWriter out, Segment segment) throws IOException {
			out.beginObject();

			out.name("id");
			out.value(segment.id);
			out.name("ip");
			out.value(segment.domain + "/" + segment.ip.getHostAddress());
			out.name("port");
			out.value(String.valueOf(segment.port));
			out.name("severity");
			out.value(segment.severity.toString());
			out.name("finding");
			out.value(segment.finding);
			if (segment.exploit.size() != 0) {
				out.name("cve");
				StringBuilder builder = new StringBuilder();
				segment.exploit.forEach(e -> builder.append(e).append(" "));
				out.value(builder.toString());
			}
			out.endObject();

		}

		@Override
		public Segment read(JsonReader in) throws IOException {

			JsonToken token = in.peek();

			if (!token.equals(JsonToken.BEGIN_OBJECT)) {
				logger.error("Malformed json data.");
				throw new IllegalArgumentException("Malformed json data");
			}

			in.beginObject();

			try {

				String id = "",
						ip = "",
						port = "",
						severity = "",
						finding = "",
						cve = "",
						cwe = "";

				while (!in.peek().equals(JsonToken.END_OBJECT)) {
					switch (in.nextName()) {
						case "id":
							id = in.nextString();
							logger.debug("ID[" + id + "] found");
							break;
						case "ip":
							ip = in.nextString();
							logger.debug("IP[" + ip + "] found");
							break;
						case "port":
							port = in.nextString();
							logger.debug("Port[" + port + "] found");
							break;
						case "severity":
							severity = in.nextString();
							logger.debug("Severity[" + severity + "] found");
							break;
						case "finding":
							finding = in.nextString();
							logger.debug("finding[" + finding + "] found");
							break;
						case "cve":
							cve = in.nextString();
							logger.debug("CVE[" + cve + "] found");
							break;
						case "cwe":
							cwe = in.nextString();
							logger.debug("CWE[" + cwe + "] found");
							break;
					}
				}

				in.endObject();

				String exploit = (cve + " " + cwe).trim();

				Segment segment;
				if (exploit.isEmpty()) segment = new Segment(id, ip, port, severity, finding);
				else segment = new Segment(id, ip, port, severity, finding, exploit);

				if (segment.severity.getLevel() == Level.getByName("FATAL").getLevel()) {
					throw new FatalTagFoundException("Fatal tag found: " + segment.getFinding());
				}
				return segment;
			} catch (Exception e) {
				logger.error("Segment parse failed, discarding result.", e);
				throw e;
			}
		}
	}
}
