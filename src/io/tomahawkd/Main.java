package io.tomahawkd;

import io.tomahawkd.common.ShodanExplorer;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.TargetSegmentMap;
import io.tomahawkd.testssl.data.parser.CommonParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.List;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		try {
			List<String> host = ShodanExplorer.explore("has_ssl:true webcam");
			for (String s : host) {
				TargetSegmentMap t = CommonParser.parseFile(ExecutionHelper.runTest(s));
				t.forEach((ip, seg) -> Analyzer.analyze(seg));
			}
		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}
}
