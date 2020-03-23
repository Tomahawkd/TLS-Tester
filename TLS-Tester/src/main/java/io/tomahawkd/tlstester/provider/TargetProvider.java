package io.tomahawkd.tlstester.provider;

import io.tomahawkd.tlstester.provider.sources.TargetSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

public class TargetProvider implements TargetStorage {

	public static final Logger logger = LogManager.getLogger(TargetProvider.class);

	private State state = State.INITIAL;
	private Deque<String> queue = new ConcurrentLinkedDeque<>();
	private ReentrantLock lock = new ReentrantLock();
	private List<TargetSource> sourceList = new ArrayList<>();

	public TargetProvider(TargetSource s) {
		this.sourceList.add(s);
	}

	public TargetProvider(List<TargetSource> sl) {
		this.sourceList = new ArrayList<>();
		this.sourceList.addAll(sl);
	}

	public void run() {
		setStatus(State.WAITING);
		new Thread(() -> {
			for (TargetSource source : sourceList) {
				try {
					source.acquire(this);
				} catch (Exception e) {
					logger.error(
							"Error occurs while acquire data from {}",
							source.getClass().getName(), e);
				}
			}
			setStatus(State.FINISHING);
		}, "DataAcquire@" + hashCode()).start();
	}

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

	public String getNextTarget() {

		try {
			lock.lock();
			while (state == State.WAITING || queue.isEmpty()) {
				setStatus(State.WAITING);
				synchronized (this) {
					wait();
				}
			}

			logger.debug("Lock acquired, getting data");
			String d = queue.pop();
			if (queue.isEmpty()) {
				if (state == State.FINISHING) setStatus(State.FINISHED);
				else setStatus(State.WAITING);
			}

			return d;
		} catch (InterruptedException e) {
			logger.fatal("Interrupted lock state.", e);
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public synchronized void add(String data) {
		queue.addLast(data);
		setStatus(State.RUNNING);
		synchronized (this) {
			notify();
		}
	}

	@Override
	public synchronized void addAll(Collection<String> data) {
		queue.addAll(data);
		setStatus(State.RUNNING);
		synchronized (this) {
			notify();
		}
	}

	public enum State {
		INITIAL, // Functionally like FINISHED, Default when initially constructed
		RUNNING, // Querying data in cache when extract remaining data
		WAITING, // Waiting data from extraction
		FINISHING, // Querying data in cache with extraction complete
		FINISHED // No more data
	}
}
