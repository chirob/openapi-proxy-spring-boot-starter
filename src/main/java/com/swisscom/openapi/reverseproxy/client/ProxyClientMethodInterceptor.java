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

package com.swisscom.openapi.reverseproxy.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.FatalBeanException;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.swisscom.openapi.reverseproxy.annotation.ProxyInterceptor;

public class ProxyClientMethodInterceptor extends ProxyMethodInterceptor<Object> {

	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	private final Object proxyOptionsBean;

	private final ProxyClient proxyClient;

	private Method interceptorMethod;

	public ProxyClientMethodInterceptor(Object proxyOptionsBean, String path, List<RequestMethod> requestMethods,
			ProxyClient proxyClient) {
		this.proxyOptionsBean = proxyOptionsBean;
		this.proxyClient = proxyClient;

		var interceptorMethods = Arrays.stream(ClassUtils.getUserClass(proxyOptionsBean).getMethods())
			.filter((method) -> Optional.ofNullable(method.getAnnotation(ProxyInterceptor.class))
				.map((annot) -> matchAnnotationPaths(annot.path(), path)
						&& matchAnnotationMethods(annot.method(), requestMethods))
				.orElse(false))
			.toArray(Method[]::new);
		if (interceptorMethods.length > 1) {
			throw new FatalBeanException(
					"Too many interceptors for the same [path, methods] pair: [" + path + ", " + requestMethods + "]");
		}
		if (interceptorMethods.length == 1) {
			this.interceptorMethod = interceptorMethods[0];
			this.interceptorMethod.setAccessible(true);
		}
	}

	@Override
	public Object invoke() {
		try {
			return Optional.ofNullable(this.interceptorMethod).map((method) -> {
				try {
					return this.proxyClient.updateResponse(method.invoke(this.proxyOptionsBean, this.proxyClient));
				}
				catch (InvocationTargetException ex) {
					throw new IllegalStateException(ex.getTargetException());
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException(ex);
				}
			}).orElseGet(() -> this.proxyClient.exchange());
		}
		finally {
			this.proxyClient.cleanRequestAttributes();
		}
	}

	protected boolean matchAnnotationMethods(RequestMethod[] annotationMethods, List<RequestMethod> requestMethods) {
		return annotationMethods.length == 0 || requestMethods.containsAll(List.of(annotationMethods))
				|| List.of(annotationMethods).containsAll(requestMethods);
	}

	protected boolean matchAnnotationPaths(String[] annotationPaths, String path) {
		return annotationPaths.length == 0 || Arrays.stream(annotationPaths)
			.anyMatch((ap) -> this.antPathMatcher.match(ap, path) || this.antPathMatcher.match(path, ap));
	}

}
