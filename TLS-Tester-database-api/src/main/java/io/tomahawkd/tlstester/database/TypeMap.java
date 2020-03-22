package io.tomahawkd.tlstester.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type mapping between database type and java type
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeMap {

	/**
	 * Integer type for database
	 * @return database int type string
	 */
	String integer() default "integer";

	/**
	 * String type for database
	 * @return database string type string
	 */
	String string() default "text";

	/**
	 * Boolean type for database
	 * @return database boolean type string
	 */
	String bool() default "boolean";
}
