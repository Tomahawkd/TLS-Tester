package io.tomahawkd.tlstester.testssl.data;

@FunctionalInterface
public interface FindingParser<ExpectResult> {

	ExpectResult parse(String findings);
}
