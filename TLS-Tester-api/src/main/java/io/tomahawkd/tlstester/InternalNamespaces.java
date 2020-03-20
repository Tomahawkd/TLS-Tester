package io.tomahawkd.tlstester;

/**
 * This class contains various namespace internally predefined. Override or
 * overlap these namespace will cause unpredictable behaviours
 */
public class InternalNamespaces {

	/**
	 * This class contains predefined analyzers implemented by referring
	 * Postcards from the Post-HTTP World: Amplification of HTTPS Vulnerabilities
	 * in the Web Ecosystem, 2019 IEEE Symposium on Security and Privacy (SP),
	 * San Francisco, CA, USA, 2019, pp. 281-298.
	 */
	public static class Analyzers {

		/**
		 * Leaky channel analyzer
		 */
		public static final String LEAKY = "leaky";

		/**
		 * Tainted channel analyzer
		 */
		public static final String TAINTED = "tainted";

		/**
		 * Partially leaky analyzer
		 */
		public static final String PARTIALLY_LEAKY = "partially_leaky";
	}

	/**
	 * This class contains Internal data tags collected by
	 * {@link io.tomahawkd.tlstester.data.DataCollector}<br>
	 * Since the Data type could be various, the data collector returns {@link Object}, and
	 * its real type has to be stored in annotation
	 * {@link io.tomahawkd.tlstester.data.DataCollectTag#type}
	 */
	public static class Data {

		/**
		 * Detect if the target service has SSL/TLS connection
		 * Type: {@link Boolean}
		 */
		public static final String HAS_SSL = "has_ssl";

		/**
		 * Acquire target information from shodan
		 * Type: {@link com.fooock.shodan.model.host.Host}
		 */
		public static final String SHODAN = "shodan";

		/**
		 * Identify target brand using shodan information
		 * Type: {@link String}
		 */
		public static final String IDENTIFIER = "identifier";

		/**
		 * Testssl scan results
		 * Type: SegmentMap (Unfortunately the testssl data structure is not extracted yet)
		 */
		public static final String TESTSSL = "testssl";
	}
}
