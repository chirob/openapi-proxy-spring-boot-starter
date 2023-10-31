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
			proxyRequestMappingHandlerMappingSupplier.get().registerMapping(
					requestMappingInfoBuilder.options(REQUEST_MAPPING_BUILDER_CONFIGURATION).build(), methodInterceptor,
					methodInterceptor.getInvocationMethod());
		} catch (BeansException | SecurityException e) {
			throw new FatalBeanException(e.getMessage(), e);
		}
	}

}
