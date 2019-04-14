package io.tomahawkd.common.log;

public class LogHandler {

	private LogLevel level;
	private FormatterDelegate formatterDelegate;
	private OutputDelegate outputDelegate;

	LogHandler(LogLevel level) {
		this.level = level;
	}

	public void setFormatter(FormatterDelegate formatter) {
		this.formatterDelegate = formatter;
	}

	public void setOutput(OutputDelegate output) {
		this.outputDelegate = output;
	}

	public void setLoggingLevel(LogLevel level) {
		this.level = level;
	}

	public LogLevel getLoggingLevel() {
		return level;
	}

	void applyMessage(LoggingRecord record) {
		if (this.level.getLevel() > record.getLevel().getLevel()) return;

		// default delegate
		if (formatterDelegate == null) formatterDelegate = LoggingRecord::toString;
		if (outputDelegate == null) outputDelegate = System.out::println;

		try {
			outputDelegate.publish(formatterDelegate.format(record));
		} catch (Exception e) {
			System.err.println(
					new LoggingRecord(LogLevel.FATAL,
							this.getClass().getName(),
							"Cannot log message"));
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface FormatterDelegate {
		String format(LoggingRecord record);
	}

	@FunctionalInterface
	public interface OutputDelegate {
		void publish(String message) throws Exception;
	}
}
