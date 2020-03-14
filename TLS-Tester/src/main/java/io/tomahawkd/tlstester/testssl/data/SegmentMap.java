package io.tomahawkd.tlstester.testssl.data;

import io.tomahawkd.tlstester.common.log.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SegmentMap {

	private static final Logger logger = Logger.getLogger(SegmentMap.class);

	private Map<Tag, Segment> segmentMap;

	public SegmentMap() {
		this.segmentMap = new LinkedHashMap<>();
	}

	public Segment get(Tag tagId) {
		return segmentMap.get(tagId);
	}

	public Segment get(String id) {
		return get(Tag.getTag(id));
	}

	public void add(Segment info) {

		logger.debug("Adding " + info);

		if (!segmentMap.containsKey(info.getTag())) segmentMap.put(info.getTag(), info);
		else segmentMap.get(info.getTag()).merge(info);
	}

	public List<Segment> getByType(SectionType type) {
		List<Segment> list = new ArrayList<>();
		forEach((k, v) -> {
			if (k.getType().equals(type)) {
				list.add(v);
			}
		});

		logger.debug("Got " + list.size() + " by type");

		return list;
	}

	public String getIp() {
		return segmentMap.values().iterator().next().getIp();
	}

	public void forEach(BiConsumer<? super Tag, ? super Segment> function) {
		segmentMap.forEach(function);
	}
}
