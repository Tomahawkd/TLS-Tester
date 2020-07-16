package io.tomahawkd.tlstester;

import io.tomahawkd.tlstester.analyzer.AnalyzerRunner;
import io.tomahawkd.tlstester.data.Callback;
import io.tomahawkd.tlstester.data.DataCollectExecutor;
import io.tomahawkd.tlstester.data.TargetInfo;
import io.tomahawkd.tlstester.database.RecorderHandler;
import io.tomahawkd.tlstester.extensions.ExtensionManager;
import io.tomahawkd.tlstester.provider.TargetProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainProcedure {

	private static final Logger logger = LogManager.getLogger(MainProcedure.class);

	public static void run(TargetInfo target, TargetProvider provider) {

		Callback pre = target.getPretest();
		if (pre != null) pre.call(target, provider);

		logger.info("Start testing host " + target.getHost());
		logger.info("Collecting necessary data...");
		ExtensionManager.INSTANCE.get(DataCollectExecutor.class).collectInfoTo(target);
		logger.info("Analyzing target...");
		ExtensionManager.INSTANCE.get(AnalyzerRunner.class).analyze(target);
		logger.info("Analyzing complete, recording results...");
		ExtensionManager.INSTANCE.get(RecorderHandler.class).getRecorder().record(target);
		logger.info("Finishing test...");

		Callback post = target.getPostTest();
		if (post != null) post.call(target, provider);

	}
}
