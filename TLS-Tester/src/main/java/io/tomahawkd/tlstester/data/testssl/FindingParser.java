package io.tomahawkd.tlstester.data.testssl;

@FunctionalInterface
public interface FindingParser<ExpectResult> {

	ExpectResult parse(String findings);
}
