package io.tomahawkd.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {

	/**
	 * Database Name. Must match to jdbc declaration
	 */
	String name();

	/**
	 * Database type(file or network service)
	 */
	DatabaseType type();

	/**
	 * Network service database host
	 */
	String host() default "";

	/**
	 * For file, extension(must start with .);<br>
	 * For network service, parameter(must start with ?)
	 */
	String extension() default "";

	boolean authenticateRequired() default false;
}
