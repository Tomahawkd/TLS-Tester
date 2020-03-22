package io.tomahawkd.tlstester.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Database delegate declaration.
 * You may use this annotation in {@link io.tomahawkd.tlstester.database.delegate.RecorderDelegate}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {

	/**
	 * Database Name, for commandline db_type matching
	 */
	String name();

	/**
	 * @return true if the database need authentication
	 */
	boolean authenticateRequired() default false;

	/**
	 * @return specific the driver which the delegate will use. Automatically choose if
	 * this field remains empty string
	 */
	String useDriver() default "";
}
