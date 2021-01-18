package io.tomahawkd.tlstester.config;

public class EnvironmentConstants {

	// SYSTEM_OS

	// String from openjdk jdk/src/java.base/windows/native/libjava/java_props_md.c
	// WINDOWS 10 has a feature (WSL) to run linux programs so we just need to identify
	// whether the host system is windows 10.
	public static final String WINDOWS = "Windows";
	public static final String WINDOWS10 = "Windows 10";

	// String from openjdk jdk/src/java.base/macosx/native/libjava/java_props_macosx.c
	public static final String MACOS = "Mac OS X";

	// Unix/Linux uses uname to acquire system name, since the various type of
	// system name, we just uses Linux
	public static final String LINUX = "Linux";
}
