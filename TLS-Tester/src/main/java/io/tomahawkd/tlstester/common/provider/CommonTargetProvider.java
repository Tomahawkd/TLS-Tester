package io.tomahawkd.tlstester.common.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CommonTargetProvider<T> implements TargetProvider<T> {

	public static final Logger logger = LogManager.getLogger(CommonTargetProvider.class);

	private State state = State.INITIAL;
	private Deque<T> queue = new ConcurrentLinkedDeque<>();

	private ReentrantLock lock = new ReentrantLock();
	private Condition emptyCondition = lock.newCondition();

	protected final void setStatus(State state) {
		lock.lock();
		logger.debug("Set state " + state.toString());
		this.state = state;
		lock.unlock();
	}

	public boolean hasMoreData() {
		return state != State.FINISHED;
	}

	public State getStatus() {
		return state;
	}

	@Override
	public T getNextTarget() {

		try {
			lock.lock();
			while (state == State.WAITING) {
				emptyCondition.await();
			}

			logger.debug("Lock acquired, getting data");
			if (queue.isEmpty()) return null;
			T d = queue.pop();
			if (queue.isEmpty()) {
				if (state == State.FINISHING) setStatus(State.FINISHED);
				else setStatus(State.WAITING);
			}

			return d;
		} catch (InterruptedException e) {
			logger.fatal("Interrupted lock state.");
			logger.fatal(e.getMessage());
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public synchronized void add(T data) {
		queue.addLast(data);
	}

	@Override
	public synchronized void addAll(Collection<T> data) {
		queue.addAll(data);
	}
}
