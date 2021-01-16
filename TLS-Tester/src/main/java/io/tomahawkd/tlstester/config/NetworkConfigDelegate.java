package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.config.AbstractConfigDelegate;
import io.tomahawkd.config.annotation.BelongsTo;
import io.tomahawkd.config.commandline.CommandlineConfig;

@BelongsTo(CommandlineConfig.class)
public class NetworkConfigDelegate extends AbstractConfigDelegate {

	@Parameter(names = "--net_thread", description =
			"Total network thread for data process to be activated.")
	private int networkThreadsCount = 5;

	public int getNetworkThreadsCount() {
		return networkThreadsCount;
	}
}
