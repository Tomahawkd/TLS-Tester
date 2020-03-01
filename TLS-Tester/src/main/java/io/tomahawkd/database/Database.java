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

	boolean authenticateRequired() default false;
}
