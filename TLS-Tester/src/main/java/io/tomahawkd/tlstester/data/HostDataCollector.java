package io.tomahawkd.tlstester.data;

import com.fooock.shodan.model.host.Host;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.netservice.ShodanQueriesHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@InternalDataCollector(order = 2)
@DataCollectTag(tag = InternalDataNamespace.SHODAN, type = Host.class)
public class HostDataCollector implements DataCollector {

	private static final Logger logger = Logger.getLogger(HostDataCollector.class);

	@Override
	public Object collect(TargetInfo host) {

		logger.debug("Start query Shodan for ip information");
		HostObserver<Host> hostObserver = new HostObserver<>();
		ShodanQueriesHelper.searchWithIp(host.getIp(), hostObserver);

		int timePassed = 0;
		while (!hostObserver.isComplete()) {
			try {
				if (timePassed > 60) {
					logger.warn("Target " + host.getIp() + " look up in Shodan failed.");
					break;
				}
				Thread.sleep(1000);
				timePassed += 1;
			} catch (InterruptedException e) {
				break;
			}
		}

		Host hostInfo = null;
		if (hostObserver.isComplete()) {
			List<Host> result = hostObserver.getResult();
			if (result.isEmpty()) logger.warn("Host query is null.");
			else hostInfo = hostObserver.getResult().get(0);
		} else {
			logger.warn("Host query timeout.");
		}
		return hostInfo;
	}

	static class HostObserver<T> extends DisposableObserver<T> {

		private static final Logger logger = Logger.getLogger(HostDataCollector.HostObserver.class);

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

}
