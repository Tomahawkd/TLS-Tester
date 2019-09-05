package io.tomahawkd.common.provider;

import io.tomahawkd.common.FileHelper;
import io.tomahawkd.common.log.Logger;
import io.tomahawkd.testssl.data.parser.CommonParser;

public class FileTargetProvider extends AbstractTargetProvider {

	public static final Logger logger = Logger.getLogger(FileTargetProvider.class);
	private TargetProvider provider;

	public FileTargetProvider(String path) {
		try {
			String data = FileHelper.readFile(path);
			provider = new ListTargetProvider(CommonParser.parseHost(data));
		} catch (Exception e) {
			this.setStatus(State.FINISHED);
			logger.critical("File operation has met exception: " + e.getMessage());

		}
	}

	@Override
	public State getStatus() {
		return provider.getStatus();
	}

	@Override
	public String getNextTarget() {
		return provider.getNextTarget();
	}
}
