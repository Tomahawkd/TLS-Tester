package io.tomahawkd.common.provider;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ListTargetProvider<T> extends AbstractTargetProvider<T> {

	private Iterator<T> iterator;

	public ListTargetProvider(List<T> list) {
		this.iterator = Objects.requireNonNull(list, "List cannot be null.")
				.iterator();
	}

	@Override
	public State getStatus() {
		return iterator.hasNext() ? State.RUNNING : State.FINISHED;
	}

	@Override
	public T getNextTarget() {
		return iterator.next();
	}
}
