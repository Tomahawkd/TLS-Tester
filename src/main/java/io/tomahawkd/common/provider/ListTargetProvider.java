package io.tomahawkd.common.provider;

import java.util.Iterator;
import java.util.List;

public class ListTargetProvider extends AbstractTargetProvider {

	private Iterator<String> iterator;

	public ListTargetProvider(List<String> ipList) {
		this.iterator = ipList.iterator();
	}

	@Override
	public State getStatus() {
		return iterator.hasNext() ? State.RUNNING : State.FINISHED;
	}

	@Override
	public String getNextTarget() {
		return iterator.next();
	}
}
