package io.tomahawkd.tlstester.common;

@FunctionalInterface
public interface ThrowableSupplier<R> {
	R get() throws Exception;
}
