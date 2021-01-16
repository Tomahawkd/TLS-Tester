package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.config.AbstractConfigDelegate;
import io.tomahawkd.config.annotation.BelongsTo;
import io.tomahawkd.config.commandline.CommandlineConfig;

@BelongsTo(CommandlineConfig.class)
public class HelpConfigDelegate extends AbstractConfigDelegate {

	@Parameter(names = {"-h", "--help"}, help = true,
			description = "Prints usage for all the existing commands.")
	private boolean help;

	public boolean isHelp() {
		return help;
	}
}
