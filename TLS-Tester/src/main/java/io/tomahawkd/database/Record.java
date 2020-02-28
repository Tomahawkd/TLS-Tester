package io.tomahawkd.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Record {

	/**
	 * Main column name
	 */
	String column();

	/**
	 * Statistic on specific position
	 */
	StatisticMapping[] map() default {};

	/**
	 * TreeCode length
	 */
	int resultLength();

	/**
	 * Sync position value for those has same cert hash
	 */
	int[] checkPosition() default {};
}
