package io.tomahawkd.common.provider.delegate;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.common.provider.ListTargetProvider;
import io.tomahawkd.common.provider.TargetProvider;
import io.tomahawkd.netservice.ShodanExplorer;

@SuppressWarnings("unused")
public class ShodanProviderDelegate implements ProviderDelegateParser {

	public static final String TYPE = "shodan";

	@Override
	public boolean identify(String type) {
		return TYPE.equalsIgnoreCase(type);
	}

	@Override
	public TargetProvider<String> parse(String v) throws Exception {
		if (!v.contains("::")) {
			return new ListTargetProvider<>(ShodanExplorer.explore(v));
		} else {
			String[] l = v.split(":", 2);
			String[] range = l[0].split("-", 2);
			int start = Integer.parseInt(range[0]);
			int count = Integer.parseInt(range[1]) - start + 1;
			if (count <= 0) throw new ParameterException("Range error");

			return new ListTargetProvider<>(ShodanExplorer.explore(l[1], start, count));
		}
	}
}