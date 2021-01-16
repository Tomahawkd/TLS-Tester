package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;
import io.tomahawkd.config.AbstractConfigDelegate;
import io.tomahawkd.config.annotation.BelongsTo;
import io.tomahawkd.config.commandline.CommandlineConfig;

@BelongsTo(CommandlineConfig.class)
public class TempFileConfigDelegate extends AbstractConfigDelegate {

	@Parameter(names = "--temp", description = "Temp file expired day. (-1 indicates forever)")
	private Integer tempExpireTime = 7;

	public Integer getTempExpireTime() {
		return tempExpireTime;
	}
}
