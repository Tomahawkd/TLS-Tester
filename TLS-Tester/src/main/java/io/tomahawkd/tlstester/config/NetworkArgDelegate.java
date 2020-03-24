package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;

public class NetworkArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--net_thread", description =
			"Total network thread for data process to be activated.")
	private int networkThreadsCount = 5;

	public int getNetworkThreadsCount() {
		return networkThreadsCount;
	}
}
