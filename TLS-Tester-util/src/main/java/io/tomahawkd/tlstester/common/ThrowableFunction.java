package io.tomahawkd.tlstester.common;

@FunctionalInterface
public interface ThrowableFunction<V, R> {
	R apply(V value) throws Exception;
}
