package io.tomahawkd.common.provider.delegate;

import io.tomahawkd.common.provider.ListTargetProvider;
import io.tomahawkd.common.provider.TargetProvider;

import java.util.Arrays;

@SuppressWarnings("unused")
public class IpProviderDelegate implements ProviderDelegateParser {

		public static final String TYPE = "ips";

		@Override
		public boolean identify(String type) {
			return TYPE.equalsIgnoreCase(type);
		}

		@Override
		public TargetProvider<String> parse(String v) {
			return new ListTargetProvider<>(Arrays.asList(v.split(";")));
		}
	}