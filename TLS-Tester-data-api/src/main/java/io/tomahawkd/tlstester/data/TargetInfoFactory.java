package io.tomahawkd.tlstester.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.stream.Collectors;

public class TargetInfoFactory {

	@Contract("_ -> new")
	public static TargetInfo defaultBuild(@NotNull InetSocketAddress host) {
		return new TargetInfo(host);
	}

	public static TargetInfo pretestBuild(@NotNull InetSocketAddress host, Callback pretest) {
		TargetInfo info = new TargetInfo(host);
		info.setPretest(pretest);
		return info;
	}

	public static TargetInfo postTestBuild(@NotNull InetSocketAddress host, Callback postTest) {
		TargetInfo info = new TargetInfo(host);
		info.setPostTest(postTest);
		return info;
	}

	public static TargetInfo customBuild(@NotNull InetSocketAddress host,
	                              Callback pretest, Callback postTest) {
		TargetInfo info = new TargetInfo(host);
		info.setPretest(pretest);
		info.setPostTest(postTest);
		return info;
	}

	public static Collection<TargetInfo> defaultBuildAll(
			@NotNull Collection<InetSocketAddress> hosts) {
		return hosts.stream()
				.map(TargetInfoFactory::defaultBuild)
				.collect(Collectors.toList());
	}

	public static Collection<TargetInfo> pretestBuildAll(
			@NotNull Collection<InetSocketAddress> hosts, Callback pretest) {
		return hosts.stream()
				.map(host -> pretestBuild(host, pretest))
				.collect(Collectors.toList());
	}

	public static Collection<TargetInfo> postTestBuildAll(
			@NotNull Collection<InetSocketAddress> hosts, Callback postTest) {
		return hosts.stream()
				.map(host -> postTestBuild(host, postTest))
				.collect(Collectors.toList());
	}

	public static Collection<TargetInfo> customBuildAll(
			@NotNull Collection<InetSocketAddress> hosts,
			Callback pretest, Callback postTest) {
		return hosts.stream()
				.map(host -> customBuild(host, pretest, postTest))
				.collect(Collectors.toList());
	}
}
