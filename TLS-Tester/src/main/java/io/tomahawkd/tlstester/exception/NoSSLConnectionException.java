package io.tomahawkd.tlstester.exception;

public class NoSSLConnectionException extends RuntimeException {

	public NoSSLConnectionException() {
		super("No ssl connection");
	}

	public NoSSLConnectionException(String message) {
		super(message);
	}
}
