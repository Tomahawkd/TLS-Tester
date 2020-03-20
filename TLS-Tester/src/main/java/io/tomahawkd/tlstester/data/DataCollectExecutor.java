package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.common.ComponentsLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public enum DataCollectExecutor {

	INSTANCE;

	private List<DataCollector> list;
	private final Logger logger = LogManager.getLogger(DataCollectExecutor.class);

	DataCollectExecutor() {
		list = new ArrayList<>();
	}

	public void init() {

		Set<Class<? extends DataCollector>> c =
				ComponentsLoader.INSTANCE.loadClasses(DataCollector.class);

		logger.debug("Loading data collectors");
		for (Class<? extends DataCollector> aClass : c) {
			logger.debug("Loading data collector " + aClass.getName());
			try {
				if (aClass.getAnnotation(DataCollectTag.class) == null)
					throw new IllegalArgumentException("No tag in class " + aClass.getName());
				list.add(aClass.newInstance());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
				logger.error("Cannot instantiate class " + aClass.toString(), e);
			}
		}

		list.sort(Comparator.comparingInt(
				e -> e.getClass().getAnnotation(DataCollectTag.class).order()));
	}

	public void collectInfoTo(TargetInfo info) {
		Map<String, Object> data = info.getCollectedData();

		for (DataCollector dataCollector : list) {
			try {
				String tag = dataCollector.getClass().getAnnotation(DataCollectTag.class).tag();
				data.put(tag, dataCollector.collect(info));
			} catch (Exception e) {
				logger.warn("Exception during collecting data using " +
						dataCollector.getClass().getName(), e);
			}
		}
	}
}
