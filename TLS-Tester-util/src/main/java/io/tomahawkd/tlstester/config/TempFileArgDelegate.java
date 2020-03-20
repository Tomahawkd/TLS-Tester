package io.tomahawkd.tlstester.config;

import com.beust.jcommander.Parameter;

public class TempFileArgDelegate extends AbstractArgDelegate {

	@Parameter(names = "--temp", description = "Temp file expired day. (-1 indicates forever)")
	private Integer tempExpireTime = 7;

	public Integer getTempExpireTime() {
		return tempExpireTime;
	}
}
