package io.tomahawkd.testssl;

@FunctionalInterface
public interface FindingParser<ExpectResult> {

	ExpectResult parse(String findings);
}
