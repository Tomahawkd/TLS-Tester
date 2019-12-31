package io.tomahawkd.common.provider;

public abstract class AbstractTargetProvider<T> implements TargetProvider<T> {

	private State state = State.INITIAL;

	protected final void setStatus(State state) {
		this.state = state;
	}

	public boolean hasMoreData() {
		return getStatus() == State.RUNNING ||
				getStatus() == State.WAITING ||
				getStatus() == State.FINISHING;
	}

	public State getStatus() {
		return state;
	}

	public abstract T getNextTarget();
}
