package com.swisscom.openapi.reverseproxy.config;

import java.util.Arrays;
import java.util.List;

import com.swisscom.openapi.reverseproxy.annotation.Proxy;
import com.swisscom.openapi.reverseproxy.util.SpelExpressionEvaluator;

import lombok.Getter;

public class AnnotationProxyOptions extends SpelEvaluatedProxyOptions {

	private final Proxy annotation;
	@Getter
	private final String beanName;

	protected AnnotationProxyOptions(SpelExpressionEvaluator evaluator, Proxy annotation, String beanName) {
		super(evaluator);
		this.annotation = annotation;
		this.beanName = beanName;
	}

	protected String specification() {
		return annotation.specification();
	}

	protected String target() {
		return annotation.target();
	}

	protected String prefix() {
		return annotation.options().prefix();
	}

	protected List<String> ignoredRequestHeaders() {
		return Arrays.stream(annotation.options().ignoredRequestHeaders()).toList();
	}

	protected List<String> ignoredResponseHeaders() {
		return Arrays.stream(annotation.options().ignoredResponseHeaders()).toList();
	}

}
