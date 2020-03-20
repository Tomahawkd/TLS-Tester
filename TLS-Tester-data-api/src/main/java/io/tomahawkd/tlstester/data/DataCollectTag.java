package io.tomahawkd.tlstester.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Collector tag for data tagging</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataCollectTag {

	/**
	 * For {@link TargetInfo#getCollectedData}'s String key.
	 */
	String tag();

	/**
	 * for hint of the {@link DataCollector#collect(TargetInfo)}'s type
	 */
	Class<?> type();

	int order();
}
