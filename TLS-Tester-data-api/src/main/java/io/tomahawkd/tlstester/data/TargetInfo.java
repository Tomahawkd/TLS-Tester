package io.tomahawkd.tlstester.data;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Host information storage class</p>
 * <p>
 * This class stores key information of the target host.
 * {@link java.net.InetSocketAddress} stores target host and port.
 * {@link TargetInfo#collectedData} stores collected data from {@link io.tomahawkd.tlstester.data.DataCollector}.
 * {@link TargetInfo#analysisResult} stores test result from io.tomahawkd.tlstester.analyzer.Analyzer.
 * </p>
 *
 * <p>
 * After acquired target host from the provider, the class is instantiated with corresponding host.
 * Next, the TLS-Tester proceeds to collect all of information using
 * {@link io.tomahawkd.tlstester.data.DataCollector}
 * and stores tag from {@link DataCollectTag} and result to {@link TargetInfo#collectedData}
 * </p>
 *
 * <p>
 * When the collecting procedure is complete, the program will launch analyze procedure with
 * all subclass of io.tomahawkd.tlstester.analyzer.Analyzer and eventually stores
 * analyzer tag using io.tomahawkd.tlstester.analyzer.Record.column()
 * and result {@link TreeCode} to {@link TargetInfo#analysisResult}
 * </p>
 *
 * <p>
 * At the recording phrase, the Recorder reads the {@link TargetInfo#analysisResult} and essential
 * data from {@link TargetInfo#collectedData} and then stores data to the database.
 * </p>
 */
public class TargetInfo {

	/**
	 * Contains target host and port.
	 *
	 * @see java.net.InetSocketAddress
	 */
	private final InetSocketAddress host;

	/**
	 * Stores data collected from {@link DataCollector}<br>
	 * Key: String tag from {@link DataCollectTag#tag}<br>
	 * Value: Data
	 */
	private final Map<String, Object> collectedData;

	/**
	 * Stores data tested from io.tomahawkd.tlstester.analyzer.Analyzer<br>
	 * Key: String tag from io.tomahawkd.tlstester.analyzer.Record.column()
	 * Value: TreeCode result of the Analyzer
	 */
	private final Map<String, TreeCode> analysisResult;

	private boolean complete = false;

	private Callback pretest = null;

	private Callback postTest = null;

	TargetInfo(InetSocketAddress host) {
		this.host = host;
		this.analysisResult = new HashMap<>();
		this.collectedData = new HashMap<>();
	}

	public Map<String, Object> getCollectedData() {
		return collectedData;
	}

	public InetSocketAddress getAddress() {
		return host;
	}

	/**
	 * Get host ip string.
	 *
	 * @return host ip.
	 */
	public String getIp() {
		return host.getAddress().getHostAddress();
	}

	/**
	 * Get host ip and port.
	 *
	 * @return host ip:port
	 */
	public String getHost() {
		return host.getAddress().getHostAddress() + ":" + host.getPort();
	}

	/**
	 * Add result from Analyzer
	 *
	 * @param name   String tag from io.tomahawkd.tlstester.analyzer.Record.column()
	 * @param result TreeCode result of the Analyzer
	 * @see TargetInfo#analysisResult
	 */
	public void addResult(String name, TreeCode result) {
		analysisResult.put(name, result);
	}

	public final boolean isComplete() {
		return complete;
	}

	public final void setComplete() {
		complete = true;
	}

	public Map<String, TreeCode> getAnalysisResult() {
		return analysisResult;
	}

	void setPretest(Callback pretest) {
		this.pretest = pretest;
	}

	void setPostTest(Callback postTest) {
		this.postTest = postTest;
	}

	public Callback getPretest() {
		return pretest;
	}

	public Callback getPostTest() {
		return postTest;
	}

	/**
	 * Default cert hash value
	 */
	public static final String CERT_HASH_NULL = "null";
}
