/*
 * Copyright 2024-2099 Swisscom (Schweiz) AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		return this.annotation.specification();
	}

	protected String target() {
		return this.annotation.target();
	}

	protected String prefix() {
		return this.annotation.options().prefix();
	}

	protected List<String> ignoredRequestHeaders() {
		return Arrays.stream(this.annotation.options().ignoredRequestHeaders()).toList();
	}

	protected List<String> ignoredResponseHeaders() {
		return Arrays.stream(this.annotation.options().ignoredResponseHeaders()).toList();
	}

}
