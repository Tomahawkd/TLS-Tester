package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.log.Logger;

import java.util.*;

public enum DataCollectExecutor {

	INSTANCE;

	private List<DataCollector> list;
	private final Logger logger = Logger.getLogger(DataCollectExecutor.class);

	DataCollectExecutor() {
		list = new ArrayList<>();
	}

	public void init() {

		List<DataCollector> external = new ArrayList<>();
		Set<Class<? extends DataCollector>> c =
				ComponentsLoader.INSTANCE.loadClasses(DataCollector.class);

		logger.debug("Loading data collectors");
		for (Class<? extends DataCollector> aClass : c) {
			logger.debug("Loading data collector " + aClass.getName());
			try {
				if (aClass.getAnnotation(DataCollectTag.class) == null)
					throw new IllegalArgumentException("No tag in class " + aClass.getName());
				if (aClass.getAnnotation(InternalDataCollector.class) == null)
					external.add(aClass.newInstance());
				else {
					InternalDataCollector i =
							aClass.getAnnotation(InternalDataCollector.class);
					list.add(aClass.newInstance());
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
				logger.critical("Cannot instantiate class " + aClass.toString());
			}
		}

		// internal data collector are ordered
		list.sort(Comparator.comparingInt(
				e -> e.getClass().getAnnotation(InternalDataCollector.class).order()));
		list.addAll(external);
	}

	public void collectInfoTo(TargetInfo info) throws Exception {
		Map<String, Object> data = info.getCollectedData();

		for (DataCollector dataCollector : list) {
			try {
				String tag = dataCollector.getClass().getAnnotation(DataCollectTag.class).tag();
				data.put(tag, dataCollector.collect(info));
			} catch (Exception e) {
				logger.warn("Exception during collecting data using " + dataCollector.toString());
				logger.warn(e.getMessage());
			}
		}
	}
}
