package io.tomahawkd.testssl.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TargetSegmentMap {

	public static final String TAG = "[TargetSegment]";
	private Map<String, SegmentMap> targetSet = new LinkedHashMap<>();

	public TargetSegmentMap() { }

	public Map<String, SegmentMap> getTargetSet() {
		return targetSet;
	}

	public SegmentMap get(String addr) {
		return targetSet.get(addr);
	}

	public void add(Segment segment) {
		if (!targetSet.containsKey(segment.getIp()))
			targetSet.put(segment.getIp(), new SegmentMap());
		targetSet.get(segment.getIp()).add(segment);
	}

	public void print() {
		targetSet.forEach((ip, map) -> {
			System.out.println("{ \"" + ip + "\": [");
			map.forEach((id, seg) -> System.out.println(seg));
			System.out.println("]}\n");
		});
	}

	public void forEach(BiConsumer<? super String, ? super SegmentMap> function) {
		targetSet.forEach(function);
	}

}
