package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.analyzer.Analyzer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyMap {

	Class<? extends Analyzer> dep();

	int pos();
}
