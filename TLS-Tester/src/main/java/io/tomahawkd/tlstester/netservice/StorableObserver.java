package io.tomahawkd.tlstester.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;

import java.util.List;

public abstract class StorableObserver extends DisposableObserver<HostReport> {

	// this is for data that already cached
	public abstract void addAll(List<String> data);
}
