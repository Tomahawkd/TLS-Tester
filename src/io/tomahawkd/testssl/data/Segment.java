package io.tomahawkd.testssl.data;

import io.tomahawkd.testssl.data.parser.CipherSuiteSet;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tomahawkd
 */
public class Segment {

	public static final String TAG = "[Segment]";

	private String id;
	private String domain;
	private InetAddress ip;
	private int port;
	private Level severity;
	private String finding;
	private List<String> exploit;
	private Tag tag;
	private Object result;

	public Segment(String id, String fqdn_ip, String port, String severity, String finding)
			throws UnknownHostException {
		this(id, fqdn_ip, port, severity, finding, null);
	}

	public Segment(String id, String fqdn_ip, String port, String severity, String finding, String exploitNo)
			throws UnknownHostException {
		try {
			this.id = id;
			this.tag = Tag.getTag(id);
			if (this.tag.getType() == SectionType.CIPHER_ORDER) {
				this.result = tag.parseCipher(id, finding);
			} else {
				if (this.tag.getType() == SectionType.UNKNOWN)
					System.out.println("Unknown tag " + id + " found");
				this.result = tag.parseData(finding);
			}
			var dnip = fqdn_ip.split("/");
			this.domain = dnip[0];
			this.ip = InetAddress.getByName(dnip[1]);
			this.port = Integer.parseInt(port);
			this.severity = Level.getByName(severity);
			this.finding = finding;
		} catch (UnknownHostException e) {
			throw new UnknownHostException(TAG + e.getMessage());
		} catch (NumberFormatException e) {
			throw new NumberFormatException(TAG + e.getMessage());
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

		if (this.tag.getType() == SectionType.CIPHER_SUITE && other.tag.getType() == SectionType.CIPHER_SUITE) {
			((CipherSuiteSet) this.result).addAll((CipherSuiteSet) other.result);
			return;
		}

		if (this.equals(other)) {
			this.severity = this.severity.getLevel() >= other.severity.getLevel() ? this.severity : other.severity;
			this.finding = this.finding + "\n" + other.finding;
		} else throw new IllegalArgumentException(TAG + " Not compatible to merge.");
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

	public Tag getTag() {
		return tag;
	}

	public Object getResult() {
		return result;
	}

	public String getExploit() {
		if (exploit == null) return "null";
		var builder = new StringBuilder();
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
}
