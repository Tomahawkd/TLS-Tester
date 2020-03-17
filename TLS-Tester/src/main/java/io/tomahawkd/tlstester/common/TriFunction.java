package io.tomahawkd.tlstester.common;

@FunctionalInterface
public interface TriFunction<T, U, W, R> {

	R apply(T t, U u, W w);
}
