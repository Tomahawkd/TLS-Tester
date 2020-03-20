package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;

public class TestsslArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--testssl", description = "Testssl path. (No slash at the end)")
	private String testsslPath = "./testssl.sh";

	public String getTestsslPath() {
		return testsslPath;
	}

}
