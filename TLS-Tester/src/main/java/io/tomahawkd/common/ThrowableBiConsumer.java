package io.tomahawkd.common;

@FunctionalInterface
public interface ThrowableBiConsumer<T, V> {

	void accept(T t, V v);
}
