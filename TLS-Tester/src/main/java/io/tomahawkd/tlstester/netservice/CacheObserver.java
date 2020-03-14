package io.tomahawkd.tlstester.netservice;

import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.common.log.Logger;

import java.io.IOException;
import java.util.function.Function;

public class CacheObserver extends DisposableObserver<HostReport> {

	private static final Logger logger = Logger.getLogger(CacheObserver.class);

	private String file;
	private Function<HostReport, String> converter;

	public static final Function<HostReport, String> DEFAULT = h -> {

		StringBuilder builder = new StringBuilder();
		h.getBanners().forEach(b ->
				builder.append(b.getIpStr()).append(":").append(b.getPort()).append("\n"));
		builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	};

	public CacheObserver(String file) {
		this(file, DEFAULT);
	}

	public CacheObserver(String file, Function<HostReport, String> converter) {
		this.file = file;
		this.converter = converter;
	}

	@Override
	public void onNext(HostReport hostReport) {
		try {
			FileHelper.writeFile(file, converter.apply(hostReport) + "\n",
					!FileHelper.isFileExist(file));
		} catch (IOException e) {
			onError(e);
		}
	}

	@Override
	public void onError(Throwable throwable) {
		logger.critical(throwable.getMessage());
	}

	@Override
	public void onComplete() {

	}
}
