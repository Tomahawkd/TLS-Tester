package io.tomahawkd;

import io.tomahawkd.testssl.Analyzer;
import io.tomahawkd.testssl.ExecutionHelper;

public class Main {

	public static void main(String[] args) {
		try {
			var t = Analyzer.parseFile(ExecutionHelper.runTest("www.baidu.com"));
			t.print();
			//t.forEach((ip, seg) -> Analyzer.analyzeLeakyChannel(seg));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
