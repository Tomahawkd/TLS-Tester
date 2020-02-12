package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.Config;
import io.tomahawkd.common.ComponentsLoader;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.data.TargetInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyzerRunner {

	private static final Logger logger = Logger.getLogger(AnalyzerRunner.class);
	private static final String path = "./result/";
	private static final String extension = ".txt";

	private List<Analyzer> analyzers;
	private StringBuilder result;

	public AnalyzerRunner() {
		analyzers = new ArrayList<>();
		result = new StringBuilder();

		loadAnalyzers();
	}

	private void loadAnalyzers() {
		logger.debug("Start loading analyzers");

		ComponentsLoader
				.loadClasses(AbstractAnalyzer.class, AnalyzerRunner.class.getPackage())
				.forEach(clazz -> {
					try {
						this.addAnalyzer(clazz.newInstance());

						logger.debug("Adding Analyzer " + clazz.getName());
					} catch (InstantiationException |
							IllegalAccessException |
							ClassCastException e) {
						logger.critical("Exception during initialize identifier: " + clazz.getName());
						logger.critical(e.getMessage());
					}
				});
	}

	public void addAnalyzer(@NotNull Analyzer analyzer) {
		addAnalyzer(analyzer, analyzer);
	}

	private void addAnalyzer(@NotNull Analyzer analyzer, @NotNull Analyzer requester) {

		for (Analyzer item : analyzers) {
			// already have one
			if (item.getClass().equals(analyzer.getClass())) {
				logger.debug("Already exist analyzer " + analyzer.getClass());
				return;
			}
		}

		outer:
		for (Class<? extends Analyzer> aClass : analyzer.getDependencies()) {
			for (Analyzer item : analyzers) {
				// already have one
				if (item.getClass().equals(aClass)) continue outer;
			}

			// check dependencies for looped reference
			if (aClass.equals(requester.getClass())) {
				logger.fatal("Loop dependency detected, abort");
				throw new IllegalArgumentException("Loop dependency detected");
			}

			try {
				addAnalyzer(aClass.newInstance(), requester);
			} catch (IllegalStateException e) {
				logger.critical(e.getMessage());
				throw e;
			} catch (InstantiationException | IllegalAccessException e) {
				logger.critical("Class " + aClass.getName() + " cannot get a instance.");
				logger.critical("Ignoring analyzer.");
				throw new IllegalStateException("Cannot instance Class " + aClass.getName());
			}
		}

		logger.debug("Adding analyzer " + analyzer.getClass());
		analyzers.add(analyzer);
	}

	public void analyze(@NotNull TargetInfo info) {

		AtomicInteger completeCounter = new AtomicInteger();
		result.append("--------------START ").append(info.getIp()).append("--------------\n");

		analyzers.forEach(e -> {
			if (e.hasDependencies()) {
				Map<Class<? extends Analyzer>, Analyzer> dependencies = new HashMap<>();

				outer:
				for (Class<? extends Analyzer> aClass : e.getDependencies()) {
					for (Analyzer item : analyzers) {
						if (item.getClass().equals(aClass)) {
							dependencies.put(aClass, item);
							continue outer;
						}
						logger.critical("Missing dependencies, abort.");
						return;
					}
				}
				e.preAnalyze(info, dependencies);
			} else {
				e.preAnalyze(info, null);
			}

			result.append("\n--------------START ")
					.append(e.getClass().getName())
					.append("--------------\n\n");
			try {
				logger.info("Analyze target " + info.getIp() + " with " + e.getClass());
				e.analyze(info);
				result.append(e.getResultDescription());
				e.postAnalyze(info);
				completeCounter.getAndIncrement();
			} catch (TransportHandlerConnectException ex) {
				result.append("Exception during analyzing\n");
				logger.critical("Exception during analyzing, assuming result is false");
				logger.critical(ex.getMessage());
			}

			result.append("\n--------------END ")
					.append(e.getClass().getName())
					.append("--------------\n");
		});

		if (completeCounter.get() <= 0) {
			logger.critical("Scan met error in all section, result is not useful");
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String file = path + dateFormat.format(new Date(System.currentTimeMillis())) + extension;
			try {
				FileHelper.writeFile(file, result.toString(), true);
				// database add record

			} catch (IOException e) {
				logger.critical("Cannot write result to file, print to console instead.");
				logger.info("Result: \n" + result.toString());
			}
		}
	}

	public void postAnalyze() {
		try {
			Config.INSTANCE.getRecorder().postUpdate();
		} catch (SQLException e) {
			logger.critical("Exception during post analysis, abort");
			logger.critical(e.getMessage());
		}
	}
}
