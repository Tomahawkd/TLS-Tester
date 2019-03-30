package io.tomahawkd;

import io.tomahawkd.testssl.Analyzer;

public class Main {

	public static void main(String[] args) {
		try {
			var t = Analyzer.parseFile("./testfile/test.json");
			t.print();
			//t.forEach((ip, seg) -> Analyzer.analyzeLeakyChannel(seg));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
