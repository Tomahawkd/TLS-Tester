package io.tomahawkd.tlstester.testssl.data.parser;

import org.jetbrains.annotations.Nullable;

public class CipherSuite {

	private int hexCode;
	private String name;
	private String keyExchange;
	private String encryption;
	private int bits;
	private String rfcName;

	CipherSuite() {
		this.hexCode = -1;
		this.name = "invalid";
		this.keyExchange = null;
		this.encryption = null;
		this.bits = -1;
		this.rfcName = null;
	}

	CipherSuite(int hex, String name, String keyExchange, String encryption, String bits, String rfcName) {
		this.hexCode = hex;
		this.name = name;
		this.keyExchange = keyExchange;
		this.encryption = encryption;
		this.bits = CommonParser.parseInt(bits, 10);
		this.rfcName = rfcName;
	}

	CipherSuite(String hexCode, String name, String keyExchange, String encryption, String bits, String rfcName) {
		this(CommonParser.parseInt(hexCode.substring(1), 16), name, keyExchange, encryption, bits, rfcName);
	}

	public int getHexCode() {
		return hexCode;
	}

	public String getName() {
		return name;
	}

	public String getKeyExchange() {
		return keyExchange;
	}

	public String getEncryption() {
		return encryption;
	}

	public int getBits() {
		return bits;
	}

	public String getRfcName() {
		return rfcName;
	}

	@Nullable
	public de.rub.nds.tlsattacker.core.constants.CipherSuite getCipherForTesting() {
		return de.rub.nds.tlsattacker.core.constants.CipherSuite.getCipherSuite(hexCode);
	}

	@Override
	public String toString() {
		return "CipherSuite{" +
				"hexCode=" + hexCode +
				", name='" + name + '\'' +
				", keyExchange='" + keyExchange + '\'' +
				", encryption='" + encryption + '\'' +
				", bits=" + bits +
				", rfcName='" + rfcName + '\'' +
				'}';
	}
}
