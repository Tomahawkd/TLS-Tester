package io.tomahawkd.testssl.data.parser;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;

import java.util.ArrayList;
import java.util.List;

public class IpObserver extends DisposableObserver<HostReport> {

	private boolean complete = false;
	private List<String> ips = new ArrayList<>();

	public List<String> getIps() {
		return ips;
	}

	@Override
	public void onNext(HostReport hostReport) {
		hostReport.getBanners().forEach(e -> ips.add(e.getIpStr()));
	}

	@Override
	public void onError(Throwable e) {
		// ignore and handle by default logger
	}

	@Override
	public void onComplete() {
		this.complete = true;
		dispose();
	}

	public boolean isComplete() {
		return this.complete;
	}
}
