package io.tomahawkd.tlstester.provider.sources;

import com.beust.jcommander.ParameterException;

@SuppressWarnings("unused")
public class ShodanSourceFactory implements TargetSourceFactory {

	public static final String TYPE = "shodan";

	@Override
	public boolean identify(String type) {
		return TYPE.equalsIgnoreCase(type);
	}

	@Override
	public TargetSource build(String args) {
		if (!args.contains("::")) {
			return new ShodanSource(args);
		} else {
			String[] l = args.split("::", 2);
			String[] range = l[0].split("-", 2);
			int start = Integer.parseInt(range[0]);
			int count = Integer.parseInt(range[1]) - start + 1;
			if (count <= 0) throw new ParameterException("Range error");

			return new ShodanSource(l[1], start, count);
		}
	}
}
