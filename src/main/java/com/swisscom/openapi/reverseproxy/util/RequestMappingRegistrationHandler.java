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

package com.swisscom.openapi.reverseproxy.util;

import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.Builder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.swisscom.openapi.reverseproxy.client.ProxyMethodInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestMappingRegistrationHandler {

	private static final BuilderConfiguration REQUEST_MAPPING_BUILDER_CONFIGURATION = new BuilderConfiguration() {
		{
			setPatternParser(new PathPatternParser());
		}
	};

	private final Supplier<RequestMappingHandlerMapping> proxyRequestMappingHandlerMappingSupplier;

	public void registerMapping(Builder requestMappingInfoBuilder, ProxyMethodInterceptor<?> methodInterceptor) {
		try {
			this.proxyRequestMappingHandlerMappingSupplier.get()
				.registerMapping(requestMappingInfoBuilder.options(REQUEST_MAPPING_BUILDER_CONFIGURATION).build(),
						methodInterceptor, methodInterceptor.getInvocationMethod());
		}
		catch (BeansException | SecurityException ex) {
			throw new FatalBeanException(ex.getMessage(), ex);
		}
	}

}
