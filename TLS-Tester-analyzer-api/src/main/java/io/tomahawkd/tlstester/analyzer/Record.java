package io.tomahawkd.tlstester.analyzer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Analyzer result for recording
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Record {

	/**
	 * Main column name
	 */
	String column();

	/**
	 * TreeCode length
	 */
	int resultLength();

	/**
	 * Statistic on specific position
	 */
	StatisticMapping[] map() default {};

	/**
	 * Sync position value for those has same cert hash
	 */
	PosMap[] posMap() default {};

	DependencyMap[] depMap() default {};
}
