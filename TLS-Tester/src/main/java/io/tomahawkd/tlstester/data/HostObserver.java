package io.tomahawkd.tlstester.data;

import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.tlstester.common.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class HostObserver<T> extends DisposableObserver<T> {

	private static final Logger logger = Logger.getLogger(HostObserver.class);

	private boolean complete = false;
	private List<T> result = new ArrayList<>();

	@Override
	public void onNext(T t) {
		result.add(t);
	}

	@Override
	public void onError(Throwable e) {
		logger.critical(e.getMessage());
		onComplete();
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
