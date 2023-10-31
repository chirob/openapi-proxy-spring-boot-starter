package com.swisscom.openapi.reverseproxy.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.openapi.reverseproxy.annotation.Proxy;
import com.swisscom.openapi.reverseproxy.client.ProxyClient;
import com.swisscom.openapi.reverseproxy.client.ProxyHttpServletRequest;
import com.swisscom.openapi.reverseproxy.client.RestOperationsProvider;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiManager;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiProvider;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiRegistry;
import com.swisscom.openapi.reverseproxy.util.RequestMappingRegistrationHandler;
import com.swisscom.openapi.reverseproxy.util.SpelExpressionEvaluator;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@AutoConfiguration
public class ProxyOptionsProcessor implements BeanPostProcessor {

	protected GenericApplicationContext applicationContext;
	private List<String> proxyAnnotatedBeanNames;
	private OpenApiManager openApiManager;

	@Lazy
	@Qualifier("proxyRestOperationsProvider")
	@Autowired
	private RestOperationsProvider proxyRestOperationsProvider;
	@Lazy
	@Qualifier("proxySpelExpressionEvaluator")
	@Autowired
	private SpelExpressionEvaluator proxySpelExpressionEvaluator;
	@Lazy
	@Qualifier("proxyRequestSupplier")
	@Autowired
	private Supplier<HttpServletRequest> proxyRequestSupplier;
	@Lazy
	@Autowired
	@Qualifier("proxyObjectMapper")
	private ObjectMapper proxyObjectMapper;
	@Lazy
	@Autowired
	private OpenAPI openApi;
	@Lazy
	@Autowired
	private Optional<OpenApiProvider> openApiProvider;
	@Lazy
	@Autowired
	private ProxyServers proxyServers;
	@Lazy
	@Autowired
	private OpenApiRegistry openApiRegistry;
	@Lazy
	@Autowired
	@Qualifier("proxyRequestMappingHandlerMappingSupplier")
	private Supplier<RequestMappingHandlerMapping> proxyRequestMappingHandlerMappingSupplier;

	@Autowired
	public void init(GenericApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		openApiManager = new OpenApiManager(
				new RequestMappingRegistrationHandler(proxyRequestMappingHandlerMappingSupplier),
				openApiProvider.orElse(() -> openApi), openApiRegistry, proxyServers);
		proxyAnnotatedBeanNames = List.of(applicationContext.getBeanNamesForAnnotation(Proxy.class));
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (applicationContext != null) {
			if (bean instanceof ProxyOptions) {
				process((ProxyOptions) bean, bean);
			} else if (proxyAnnotatedBeanNames.contains(beanName)) {
				process(new AnnotationProxyOptions(proxySpelExpressionEvaluator,
						applicationContext.findAnnotationOnBean(beanName, Proxy.class), beanName), bean);
			}
		}
		return bean;
	}

	protected void process(ProxyOptions proxyOptions, Object proxyOptionsBean) throws BeansException {
		var trimProxyOptions = new TrimProxyOptions(proxyOptions);
		var proxyResourceLoader = new ProxyResourceLoader(trimProxyOptions, applicationContext,
				proxyRestOperationsProvider);
		openApiManager.parse(trimProxyOptions, proxyResourceLoader, (po, oa) -> buildProxyClient(po, oa))
				.registerPathMappings(proxyOptionsBean);
	}

	protected ProxyClient buildProxyClient(ProxyOptions proxyOptions, OpenAPI openApi) {
		var target = getTarget(openApi, proxyOptions);
		var restOperations = proxyRestOperationsProvider.getRestOperations(target);
		return new ProxyClient(proxyRestOperationsProvider.isRetryEnabled(target), proxyObjectMapper, restOperations,
				() -> new ProxyHttpServletRequest(proxyRequestSupplier.get(), proxyOptions.getPrefix()),
				proxyOptions.getIgnoredRequestHeaders(), proxyOptions.getIgnoredResponseHeaders());
	}

	protected String getTarget(OpenAPI openApi, ProxyOptions proxyOptions) {
		return Optional.ofNullable(proxyOptions.getTarget()).filter(target -> !target.isBlank())
				.orElseGet(() -> Optional.ofNullable(openApi)
						.map(oa -> oa.getServers().stream().map(server -> server.getUrl()).findFirst().orElse(null))
						.orElseThrow(() -> new IllegalArgumentException("No target URL found")));
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class TrimProxyOptions implements ProxyOptions {
		private final ProxyOptions proxyOptions;

		@Override
		public String getSpecification() {
			return trim(proxyOptions.getSpecification());
		}

		@Override
		public String getPrefix() {
			return trim(proxyOptions.getPrefix());
		}

		@Override
		public String getTarget() {
			return trim(proxyOptions.getTarget());
		}

		@Override
		public List<String> getIgnoredRequestHeaders() {
			return Optional.ofNullable(proxyOptions.getIgnoredRequestHeaders())
					.map(headers -> headers.stream().map(TrimProxyOptions::trim).toList()).orElse(null);
		}

		@Override
		public List<String> getIgnoredResponseHeaders() {
			return Optional.ofNullable(proxyOptions.getIgnoredResponseHeaders())
					.map(headers -> headers.stream().map(TrimProxyOptions::trim).toList()).orElse(null);
		}

		private static String trim(String value) {
			return Optional.ofNullable(value).map(v -> v.trim()).orElse(null);
		}
	}

}
