package io.tomahawkd.identifier;

import io.reactivex.observers.DisposableObserver;

import java.util.ArrayList;
import java.util.List;

public class HostObserver<T> extends DisposableObserver<T> {

	private boolean complete = false;
	private List<T> result = new ArrayList<>();

	@Override
	public void onNext(T t) {
		result.add(t);
	}

	@Override
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	@Override
	public void onComplete() {
		complete = true;
	}

	public boolean isComplete() {
		return complete;
	}

	public List<T> getResult() {
		return result;
	}
}
