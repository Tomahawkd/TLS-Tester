package io.tomahawkd.tlstester.provider.sources;

import com.beust.jcommander.ParameterException;
import com.fooock.shodan.model.host.HostReport;
import io.tomahawkd.tlstester.InternalNamespaces;
import io.tomahawkd.tlstester.netservice.ShodanExplorer;
import io.tomahawkd.tlstester.netservice.StorableObserver;
import io.tomahawkd.tlstester.provider.TargetStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Source(name = InternalNamespaces.Sources.SHODAN)
public class ShodanSource extends AbstractTargetSource {

	private static final Logger logger = LogManager.getLogger(ShodanSource.class);

	private String query;
	private int page;
	private int count;

	public ShodanSource(String args) {
		super(args);
		if (!args.contains("::")) {
			this.page = 1;
			this.count = 1;
			this.query = args;
		} else {
			String[] l = args.split("::", 2);
			String[] range = l[0].split("-", 2);
			this.page = Integer.parseInt(range[0]);
			this.count = Integer.parseInt(range[1]) - this.page + 1;
			if (count <= 0) throw new ParameterException("Range error");
			this.query = l[1];
		}
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
