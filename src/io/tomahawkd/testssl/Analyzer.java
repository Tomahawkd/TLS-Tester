package io.tomahawkd.testssl;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.detect.LeakyChannelAnalyzer;
import io.tomahawkd.detect.PartiallyLeakyChannelAnalyzer;
import io.tomahawkd.detect.TaintedChannelAnalyzer;
import io.tomahawkd.testssl.data.SegmentMap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Analyzer {

	private static final Logger logger = Logger.getLogger(Analyzer.class);

	private static final String path = "./result/";
	private static final String extension = ".txt";

	static {
		try {
			if (!FileHelper.isDirExist(path)) FileHelper.createDir(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create result directory");
		}
	}

	public static void analyze(SegmentMap target) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String file = path + dateFormat.format(new Date(System.currentTimeMillis())) + extension;

		StringBuilder builder = new StringBuilder("--------------START " + target.getIp() + "--------------\n");

		LeakyChannelAnalyzer leakyChannelAnalyzer = new LeakyChannelAnalyzer();
		boolean leakyResult = leakyChannelAnalyzer.checkVulnerability(target);
		if (leakyResult)
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

		builder.append(leakyChannelAnalyzer.getResult()).append("\n");


		TaintedChannelAnalyzer taintedChannelAnalyzer = new TaintedChannelAnalyzer(leakyResult);
		if (taintedChannelAnalyzer.checkVulnerability(target)) {
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		}
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

		builder.append(taintedChannelAnalyzer.getResult()).append("\n");


		PartiallyLeakyChannelAnalyzer partiallyLeakyChannelAnalyzer = new PartiallyLeakyChannelAnalyzer();
		if (partiallyLeakyChannelAnalyzer.checkVulnerability(target))
			logger.warn("Result: " + target.getIp() + " is vulnerable.");
		else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

		builder.append(partiallyLeakyChannelAnalyzer.getResult()).append("\n");
		builder.append("--------------END ").append(target.getIp()).append("--------------\n");

		try {
			FileHelper.writeFile(file, builder.toString(), true);
		} catch (IOException e) {
			logger.critical("Cannot write result to file, print to console instead.");
			logger.info("Result: \n" + builder.toString());
		}
	}
}
