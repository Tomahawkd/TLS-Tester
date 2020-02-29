package io.tomahawkd.common.provider;

import java.util.List;

public class ListTargetProvider<T> extends CommonTargetProvider<T> {

	public ListTargetProvider() {
		setStatus(State.WAITING);
	}

	public ListTargetProvider(List<T> list) {
		addAll(list);
		setStatus(State.FINISHING);
	}
}
