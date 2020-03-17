package io.tomahawkd.tlstester.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class IpObserver extends DisposableObserver<HostReport> {

	private static final Logger logger = LogManager.getLogger(IpObserver.class);

	private boolean complete = false;
	protected List<String> ips = new ArrayList<>();

	public List<String> getResult() {
		return ips;
	}

	@Override
	public void onNext(HostReport hostReport) {
		hostReport.getBanners().forEach(e -> ips.add(e.getIpStr()));
	}

	@Override
	public void onError(Throwable e) {
		logger.error(e.getMessage());
		onComplete();
	}

	@Override
	public void onComplete() {
		this.complete = true;
		dispose();
	}

	public boolean isComplete() {
		return this.complete;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		ips.forEach(e -> builder.append(e).append("\n"));
		return builder.toString();
	}
}
