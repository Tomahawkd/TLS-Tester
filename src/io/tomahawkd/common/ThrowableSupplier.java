package io.tomahawkd.common;

@FunctionalInterface
public interface ThrowableSupplier<R> {

	R get() throws Exception;
}
