package io.tomahawkd.tlstester.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {

	/**
	 * Database Name.
	 */
	String name();

	boolean authenticateRequired() default false;

	String useDriver() default "";
}
