package io.tomahawkd.tlstester.data;

import io.tomahawkd.tlstester.extensions.ExtensionHandler;
import io.tomahawkd.tlstester.extensions.ExtensionPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DataCollectExecutor implements ExtensionHandler {

	private final List<DataCollector> list;
	private final Logger logger = LogManager.getLogger(DataCollectExecutor.class);

	public DataCollectExecutor() {
		list = new ArrayList<>();
	}

	@Override
	public boolean canAccepted(Class<? extends ExtensionPoint> clazz) {
		return DataCollector.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean accept(ExtensionPoint extension) {
		if (extension.getClass().getAnnotation(DataCollectTag.class) == null) {
			logger.error("No tag in class " + extension.getClass().getName());
			return false;
		}

		logger.debug("Adding DataCollector {}", extension.getClass().getName());
		list.add((DataCollector) extension);
		return true;
	}

	@Override
	public void postInitialization() {
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
