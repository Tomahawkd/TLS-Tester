package io.tomahawkd;

import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			var t = Analyzer.parseFile(ExecutionHelper.runTest("www.baidu.com"));
			t.print();
			//t.forEach((ip, seg) -> Analyzer.analyzeLeakyChannel(seg));

		} catch (Exception e) {
			logger.fatal("Unhandled Exception");
			e.printStackTrace();
		}
	}
}
