package com.swisscom.openapi.reverseproxy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.openapi.reverseproxy.client.ProxyRestOperationProvider;
import com.swisscom.openapi.reverseproxy.client.RestOperationsProvider;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiProvider;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiRegistry;
import com.swisscom.openapi.reverseproxy.util.SpelExpressionEvaluator;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;

@AutoConfiguration
public class ProxyConfiguration {

	@ConditionalOnMissingBean(name = "proxyRestOperationsProvider")
	@Bean
	public RestOperationsProvider proxyRestOperationsProvider(Optional<RestTemplateBuilder> restTemplateBuilder) {
		return new ProxyRestOperationProvider(restTemplateBuilder);
	}

	@ConditionalOnMissingBean(name = "proxySpelExpressionEvaluator")
	@Bean
	public SpelExpressionEvaluator proxySpelExpressionEvaluator(BeanFactory beanFactory) {
		return new SpelExpressionEvaluator(beanFactory);
	}

	@ConditionalOnMissingBean(name = "proxyObjectMapper")
	@Bean
	public ObjectMapper proxyObjectMapper(ConfigurableListableBeanFactory beanFactory) {
		var objectMapperNames = new ArrayList<>(List.of(beanFactory.getBeanNamesForType(ObjectMapper.class)));
		objectMapperNames.remove("proxyObjectMapper");
		return objectMapperNames.isEmpty() ? new ObjectMapper()
				: (ObjectMapper) beanFactory.getBean((String) objectMapperNames.get(0));
	}

	@ConditionalOnMissingBean(name = "proxyRequestSupplier")
	@Bean
	public Supplier<HttpServletRequest> proxyRequestSupplier(HttpServletRequest request) {
		return () -> request;
	}

	@Bean
	public Supplier<RequestMappingHandlerMapping> proxyRequestMappingHandlerMappingSupplier(
			@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping) {
		return () -> requestMappingHandlerMapping;
	}

	@ConditionalOnMissingBean({ OpenAPI.class, OpenApiProvider.class })
	@Bean
	public OpenAPI proxyOpenApi() {
		return new OpenAPI();
	}

	@ConditionalOnMissingBean
	@Bean
	public ProxyServers proxyServers() {
		return new ProxyServers();
	}

	@ConditionalOnMissingBean
	@Bean
	public OpenApiRegistry proxyOptionsRegistry() {
		return new OpenApiRegistry();
	}

}
