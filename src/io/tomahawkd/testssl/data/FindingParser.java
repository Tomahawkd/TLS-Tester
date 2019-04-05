package io.tomahawkd.testssl.data;

@FunctionalInterface
public interface FindingParser<ExpectResult> {

	ExpectResult parse(String findings);
}
