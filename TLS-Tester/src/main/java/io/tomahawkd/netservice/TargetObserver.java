package io.tomahawkd.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.tomahawkd.common.log.Logger;

public class TargetObserver extends IpObserver {

	private static final Logger logger = Logger.getLogger(TargetObserver.class);

	@Override
	public void onNext(HostReport hostReport) {
		hostReport.getBanners().forEach(b -> ips.add(b.getIpStr() + ":" + b.getPort()));
	}

	@Override
	public void onError(Throwable e) {
		logger.critical(e.getMessage());
		onComplete();
	}
}
