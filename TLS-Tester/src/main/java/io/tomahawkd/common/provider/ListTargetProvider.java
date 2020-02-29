package io.tomahawkd.common.provider;

import java.util.Collection;
import java.util.List;

public class ListTargetProvider<T> extends CommonTargetProvider<T> {

	public ListTargetProvider() {
		setStatus(State.WAITING);
	}

	public ListTargetProvider(List<T> list) {
		addAll(list);
		setStatus(State.FINISHING);
	}

	@Override
	public synchronized void addAll(Collection<T> data) {
		super.addAll(data);
		setStatus(State.RUNNING);
	}

	public void setFinish() {
		setStatus(State.FINISHING);
	}
}
