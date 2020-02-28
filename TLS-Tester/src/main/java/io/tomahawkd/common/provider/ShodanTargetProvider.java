package io.tomahawkd.common.provider;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.common.log.Logger;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ShodanTargetProvider
		extends DisposableObserver<HostReport> implements TargetProvider<String> {

	private static final Logger logger = Logger.getLogger(ShodanTargetProvider.class);

	private State state = State.INITIAL;
	private Deque<String> queue = new ConcurrentLinkedDeque<>();
	private StringBuilder builder = new StringBuilder();

	private ReentrantLock lock = new ReentrantLock();
	private Condition emptyCondition = lock.newCondition();

	@Override
	public boolean hasMoreData() {
		return !queue.isEmpty();
	}

	@Override
	public State getStatus() {
		return this.state;
	}

	public void addAll(List<String> data) {
		queue.addAll(data);
	}

	public void setFinish() {
		lock.lock();
		state = State.FINISHING;
		lock.unlock();
	}

	public void setRunning() {
		lock.lock();
		state = State.RUNNING;
		lock.unlock();
	}

	@Override
	public String getNextTarget() {

		try {
			lock.lock();
			while (state == State.WAITING) {
				emptyCondition.await();
			}

			String d = queue.pop();
			if (queue.isEmpty()) {
				if (state == State.FINISHING) state = State.FINISHED;
				else state = State.WAITING;
			}

			return d;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void onNext(HostReport hostReport) {
		lock.lock();
		if (state == State.FINISHED) {
			lock.unlock();
			return;
		}
		hostReport.getBanners().forEach(b -> {
			queue.addLast(b.getIpStr() + ":" + b.getPort());
			builder.append(b.getIpStr()).append(":").append(b.getPort()).append("\n");
		});
		lock.unlock();
	}

	@Override
	public void onError(Throwable throwable) {
		logger.critical(throwable.getMessage());
	}

	@Override
	public void onComplete() {

	}

	public String getData() {
		return builder.toString();
	}
}
