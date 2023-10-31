package com.swisscom.openapi.reverseproxy.config;

import java.util.List;
import java.util.Optional;

import com.swisscom.openapi.reverseproxy.util.SpelExpressionEvaluator;

public abstract class SpelEvaluatedProxyOptions implements ProxyOptions {

	private SpelExpressionEvaluator evaluator;

	public final String getSpecification() {
		return evaluator.evaluate(specification());
	}

	public final String getPrefix() {
		return evaluator.evaluate(prefix());
	}

	public final String getTarget() {
		return evaluator.evaluate(target());
	}

	public final List<String> getIgnoredRequestHeaders() {
		return Optional.ofNullable(ignoredRequestHeaders())
				.map(headers -> headers.stream().map(hn -> evaluator.evaluate(hn)).toList()).orElse(List.of());
	}

	public final List<String> getIgnoredResponseHeaders() {
		return Optional.ofNullable(ignoredResponseHeaders())
				.map(headers -> headers.stream().map(hn -> evaluator.evaluate(hn)).toList()).orElse(List.of());
	}

	protected SpelEvaluatedProxyOptions(SpelExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	protected abstract String specification();

	protected abstract String prefix();

	protected abstract String target();

	protected abstract List<String> ignoredRequestHeaders();

	protected abstract List<String> ignoredResponseHeaders();

}
