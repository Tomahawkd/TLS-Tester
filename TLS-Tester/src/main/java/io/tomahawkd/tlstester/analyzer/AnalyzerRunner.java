package io.tomahawkd.tlstester.analyzer;

import io.tomahawkd.tlstester.common.ComponentsLoader;
import io.tomahawkd.tlstester.common.FileHelper;
import io.tomahawkd.tlstester.common.log.Logger;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.database.DependencyMap;
import io.tomahawkd.tlstester.database.Record;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public enum AnalyzerRunner {

	INSTANCE;

	private final Logger logger = Logger.getLogger(AnalyzerRunner.class);
	private static final String path = "./result/";
	private static final String extension = ".txt";

	private List<Analyzer> analyzers;

	AnalyzerRunner() {
		analyzers = new ArrayList<>();
	}

	public void init() {
		loadAnalyzers();
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			logger.fatal("Result directory cannot be created.");
		}
	}

	private void loadAnalyzers() {
		logger.debug("Start loading analyzers");

		ComponentsLoader.INSTANCE
				.loadClasses(Analyzer.class, AnalyzerRunner.class.getPackage())
				.forEach(clazz -> {
					try {

						// ignore abstract class
						if (Modifier.isAbstract(clazz.getModifiers())) return;
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

		DependencyMap[] d = analyzer.getClass().getAnnotation(Record.class).depMap();

		if (d.length != 0) {
			outer:
			for (DependencyMap map : d) {
				Class<? extends Analyzer> aClass = map.dep();
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
		}

		logger.debug("Adding analyzer " + analyzer.getClass());
		analyzers.add(analyzer);
	}

	public void analyze(@NotNull TargetInfo info) {

		if (!info.isHasSSL()) {
			logger.info("Target do not have a valid SSL/TLS connection, skipping.");
			return;
		}

		AtomicInteger completeCounter = new AtomicInteger();
		StringBuilder result = new StringBuilder();
		result.append("--------------START ").append(info.getIp()).append("--------------\n");

		analyzers.forEach(e -> {

			Record d = e.getClass().getAnnotation(Record.class);
			int length = d.resultLength();
			DependencyMap[] map = d.depMap();
			TreeCode code = new TreeCode(length);
			if (map.length != 0) {
				Map<Class<? extends Analyzer>, TreeCode> dependencies = new HashMap<>();

				outer:
				for (DependencyMap m : map) {
					Class<? extends Analyzer> aClass = m.dep();
					for (Analyzer item : analyzers) {
						if (item.getClass().equals(aClass)) {
							dependencies.put(aClass,
									info.getAnalysisResult()
											.get(item.getClass()
													.getAnnotation(Record.class).column()));
							continue outer;
						}
					}
					logger.critical("Missing dependencies, abort.");
					return;
				}
				e.preAnalyze(info, dependencies, code);
			} else {
				e.preAnalyze(info, null, code);
			}

			result.append("\n--------------START ")
					.append(e.getClass().getName())
					.append("--------------\n\n");
			try {
				logger.info("Analyze target " + info.getIp() + " with " + e.getClass());
				e.analyze(info, code);
				result.append(e.getResultDescription(code));
				e.postAnalyze(info, code);

				// add record
				info.addResult(e.getClass().getAnnotation(Record.class).column(), code);
				completeCounter.getAndIncrement();
			} catch (Exception ex) {
				result.append("Exception during analyzing\n");
				logger.critical("Exception during analyzing, assuming result is false");
				logger.critical(ex.getMessage());
				code.clear();
				info.addResult(e.getClass().getAnnotation(Record.class).column(), code);

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
			} catch (IOException e) {
				logger.critical("Cannot write result to file, print to console instead.");
				logger.info("Result: \n" + result.toString());
			}
		}
	}

	public void updateResult(Map<String, TreeCode> result) {
		analyzers.forEach(e -> {
			Record d = e.getClass().getAnnotation(Record.class);
			TreeCode c = result.get(d.column());
			if (c == null) {
				logger.critical("Missing result, abort.");
				return;
			}

			// handle dependency
			if (d.depMap().length != 0) {
				for (DependencyMap item : d.depMap()) {
					TreeCode depRes = result.get(item.dep().getAnnotation(Record.class).column());

					Analyzer dep = null;
					for (Analyzer analyzer : analyzers) {
						if (analyzer.getClass().equals(item.dep())) {
							dep = analyzer;
							break;
						}
					}

					if (depRes == null || dep == null) {
						logger.warn("Missing dependencies, assuming false.");
						continue;
					}

					if (dep.getResult(depRes)) {
						c.set(true, item.pos());
					}
				}
			}

			c = e.updateResult(c);
			result.put(d.column(), c);
		});
	}
}