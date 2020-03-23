package io.tomahawkd.tlstester.provider.sources;

import com.fooock.shodan.model.host.HostReport;
import io.tomahawkd.tlstester.netservice.ShodanExplorer;
import io.tomahawkd.tlstester.netservice.StorableObserver;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class ShodanSource implements TargetSource {

	private static final Logger logger = LogManager.getLogger(ShodanSource.class);

	private String query;
	private int page;
	private int count;

	public ShodanSource(String query) {
		this.query = query;
		this.page = 1;
		this.count = 1;
	}

	public ShodanSource(String query, int page, int count) {
		this.query = query;
		this.page = page;
		this.count = count;
	}

	@Override
	public void acquire(TargetStorage storage) {
		ShodanSourceObserver observer = new ShodanSourceObserver(storage);
		try {
			ShodanExplorer.explore(query, page, count, observer);
		} catch (Exception e) {
			logger.error("Error occurs in shodan exploration", e);
		}
	}

	private static class ShodanSourceObserver extends StorableObserver {

		private static final Logger logger = LogManager.getLogger(ShodanSourceObserver.class);

		private TargetStorage storage;

		private ShodanSourceObserver(TargetStorage storage) {
			this.storage = storage;
		}

		@Override
		public void onNext(HostReport hostReport) {
			this.storage.addAll(hostReport.getBanners().stream()
					.map(b -> b.getIpStr() + ":" + b.getPort())
					.peek(b -> logger.debug("adding target " + b))
					.collect(Collectors.toList()));
		}

		@Override
		public void onError(Throwable throwable) {
			logger.error(throwable.getMessage());
			onComplete();
		}

		@Override
		public void onComplete() {

		}

		@Override
		public void addAll(List<String> data) {
			this.storage.addAll(data);
		}
	}
}
