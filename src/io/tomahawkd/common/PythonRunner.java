package io.tomahawkd.common;

import io.tomahawkd.testssl.ExecutionHelper;
import io.tomahawkd.testssl.data.parser.CommonParser;

import java.io.IOException;
import java.util.List;

public class PythonRunner {

	private static final String executor = "python3 ./src/io/tomahawkd/common/ip.py";
	public static final String TAG = "[PythonRunner]";

	public static List<String> searchForSameCert(String fingerprint)
			throws IOException, InterruptedException {
		return CommonParser.parseHost(ExecutionHelper.run(executor + " " + fingerprint));
	}
}
