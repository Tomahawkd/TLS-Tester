package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.tlstester.common.FileHelper;

public class TestsslArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--testssl", description = "Testssl path. (No slash at the end)")
	private String testsslPath = "./testssl.sh";

	public String getTestsslPath() {
		return testsslPath;
	}

	@Override
	public void postParsing() {
		if (testsslPath.endsWith("/"))
			testsslPath = testsslPath.substring(0, testsslPath.length() - 1);

		if (!FileHelper.isFileExist(testsslPath + "/testssl.sh")) {
			throw new IllegalArgumentException("testssl main program not found");
		}

		if (!FileHelper.isFileExist(testsslPath + "/openssl-iana.mapping.html")) {
			throw new IllegalArgumentException("testssl name mapping not found");
		}
	}
}