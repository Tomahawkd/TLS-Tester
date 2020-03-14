package io.tomahawkd.tlstester.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PosMap {

	/**
	 * Update boolean source position
	 */
	int src();

	/**
	 * Update boolean destination position
	 */
	int dst();
}
