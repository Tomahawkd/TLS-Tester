package io.tomahawkd.common.provider;

import com.beust.jcommander.ParameterException;
import io.tomahawkd.common.ComponentsLoader;
import io.tomahawkd.netservice.ShodanExplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TargetProviderDelegate {

	public static TargetProvider<String> convert(String s) {
		List<ProviderDelegateParser> parsers = new ArrayList<>();

		Set<Class<? extends ProviderDelegateParser>> pd =
				ComponentsLoader.loadClasses(ProviderDelegateParser.class,
						TargetProviderDelegate.class.getPackage());

		for (Class<? extends ProviderDelegateParser> c : pd) {
			try {
				parsers.add(c.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Parser " + c.getName() + " instantiation failed");
			}
		}

		String[] l = s.split("::", 2);
		if (l.length != 2) throw new ParameterException("Malformed format");

		for (ProviderDelegateParser p : parsers) {
			try {
				if (p.identify(l[0])) {
					return p.parse(l[1]);
				}
			} catch (ParameterException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new ParameterException("Target type not found");
	}

	interface ProviderDelegateParser {
		boolean identify(String type);

		TargetProvider<String> parse(String v) throws Exception;
	}

	@SuppressWarnings("unused")
	static class ShodanProviderDelegate implements ProviderDelegateParser {

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

	@SuppressWarnings("unused")
	static class FileProviderDelegate implements ProviderDelegateParser {

		public static final String TYPE = "file";

		@Override
		public boolean identify(String type) {
			return TYPE.equalsIgnoreCase(type);
		}

		@Override
		public TargetProvider<String> parse(String v) {
			return FileTargetProvider.getDefault(v);
		}
	}

	@SuppressWarnings("unused")
	static class IpProviderDelegate implements ProviderDelegateParser {

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
}
