package io.tomahawkd.tlstester.data;

public interface DataCollector {

	Object collect(TargetInfo host) throws Exception;
}
