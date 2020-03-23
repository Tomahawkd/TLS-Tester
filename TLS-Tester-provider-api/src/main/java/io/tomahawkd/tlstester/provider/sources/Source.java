package io.tomahawkd.tlstester.provider.sources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Commandline source declaration
 * You may use this annotation for commandline tag process
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Source {

	/**
	 * @return source name for commandline tag
	 */
	String name();
}
