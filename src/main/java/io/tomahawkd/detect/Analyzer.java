package io.tomahawkd.detect;

import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import io.tomahawkd.Config;
import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.SegmentMap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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

		int complete = 0;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String file = path + dateFormat.format(new Date(System.currentTimeMillis())) + extension;

		StringBuilder builder = new StringBuilder("--------------START " + target.getIp() + "--------------\n");

		builder.append("\n--------------START LeakyChannel--------------\n\n");

		boolean leakyResult = false;
		try {
			LeakyChannelAnalyzer leakyChannelAnalyzer = new LeakyChannelAnalyzer();

			leakyResult = leakyChannelAnalyzer.checkVulnerability(target);

			if (leakyResult)
				logger.warn("Result: " + target.getIp() + " is vulnerable.");
			else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

			builder.append(leakyChannelAnalyzer.getResult());
			complete++;
		} catch (TransportHandlerConnectException e) {
			builder.append("Exception during Leaky Channel testing\n");
			logger.critical("Exception during Leaky Channel testing, assuming result is false");
			logger.critical(e.getMessage());
		}
		builder.append("\n--------------END LeakyChannel--------------\n");


		builder.append("\n--------------START TaintedChannel--------------\n\n");

		boolean taintedResult = false;
		try {
			TaintedChannelAnalyzer taintedChannelAnalyzer = new TaintedChannelAnalyzer(leakyResult);

			taintedResult = taintedChannelAnalyzer.checkVulnerability(target);
			if (taintedResult)
				logger.warn("Result: " + target.getIp() + " is vulnerable.");
			else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

			builder.append(taintedChannelAnalyzer.getResult());
			complete++;
		} catch (TransportHandlerConnectException e) {
			builder.append("Exception during Tainted Channel testing\n");
			logger.critical("Exception during Tainted Channel testing, assuming result is false");
			logger.critical(e.getMessage());
		}
		builder.append("\n--------------END TaintedChannel--------------\n");


		builder.append("\n--------------START PartiallyLeakyChannel--------------\n\n");

		boolean partialResult = false;
		try {
			PartiallyLeakyChannelAnalyzer partiallyLeakyChannelAnalyzer = new PartiallyLeakyChannelAnalyzer();

			partialResult = partiallyLeakyChannelAnalyzer.checkVulnerability(target);
			if (partialResult)
				logger.warn("Result: " + target.getIp() + " is vulnerable.");
			else logger.ok("Result: " + target.getIp() + " is not vulnerable.");

			builder.append(partiallyLeakyChannelAnalyzer.getResult());
			complete++;
		} catch (TransportHandlerConnectException e) {
			builder.append("Exception during Partially Leaky Channel testing\n");
			logger.critical("Exception during Partially Leaky Channel testing, assuming result is false");
			logger.critical(e.getMessage());
		}
		builder.append("\n--------------END PartiallyLeakyChannel--------------\n");


		builder.append("\n--------------END ").append(target.getIp()).append("--------------\n\n");

		try {
			if (complete > 0){
				FileHelper.writeFile(file, builder.toString(), true);

				String hash = Objects.requireNonNull(((String) target.get("cert_fingerprintSHA256").getResult()));
				Config.getRecorder()
						.addRecord(target.getIp(), true, leakyResult, taintedResult, partialResult, hash);

				if (complete != 3) logger.warn("Scan is not complete");
			} else {
				logger.critical("Scan met error in all section, result is not useful");
			}
		} catch (IOException e) {
			logger.critical("Cannot write result to file, print to console instead.");
			logger.info("Result: \n" + builder.toString());
		} catch (ClassCastException | NullPointerException e) {
			logger.critical("Cert hash value cannot be unwrapped. Skipping logging");
		}
	}
}
