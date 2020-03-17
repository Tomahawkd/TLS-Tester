package io.tomahawkd.tlstester.common.provider;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.stream.Collectors;

public class ShodanTargetProvider
		extends DisposableObserver<HostReport> implements TargetProvider<String> {

	private static final Logger logger = LogManager.getLogger(ShodanTargetProvider.class);

	private CommonTargetProvider<String> provider = new CommonTargetProvider<>();

	@Override
	public boolean hasMoreData() {
		return provider.hasMoreData();
	}

	@Override
	public State getStatus() {
		return provider.getStatus();
	}

	@Override
	public void add(String data) {
		provider.add(data);
	}

	@Override
	public void addAll(Collection<String> data) {
		provider.addAll(data);
	}

	public void setFinish() {
		provider.setStatus(State.FINISHING);
	}

	public void setRunning() {
		provider.setStatus(State.RUNNING);
	}

	@Override
	public String getNextTarget() {
		return provider.getNextTarget();
	}

	@Override
	public void onNext(HostReport hostReport) {
		if (provider.getStatus() == State.FINISHED) {
			return;
		}
		addAll(hostReport.getBanners().stream()
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
		setFinish();
	}

}
