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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
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

@Component
public class ProxyOptionsProcessor implements BeanPostProcessor {

    protected GenericApplicationContext applicationContext;

    private List<String> proxyAnnotatedBeanNames;

    private OpenApiManager openApiManager;

    @Lazy
    @Autowired
    private RestOperationsProvider proxyRestOperationsProvider;

    @Lazy
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
        this.openApiManager = new OpenApiManager(
                new RequestMappingRegistrationHandler(this.proxyRequestMappingHandlerMappingSupplier),
                this.openApiProvider.orElse(() -> this.openApi), this.openApiRegistry, this.proxyServers);
        this.proxyAnnotatedBeanNames = List.of(applicationContext.getBeanNamesForAnnotation(Proxy.class));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (this.applicationContext != null) {
            if (bean instanceof ProxyOptions) {
                process((ProxyOptions) bean, bean);
            }
            else if (this.proxyAnnotatedBeanNames.contains(beanName)) {
                process(new AnnotationProxyOptions(this.proxySpelExpressionEvaluator,
                        this.applicationContext.findAnnotationOnBean(beanName, Proxy.class), beanName), bean);
            }
        }
        return bean;
    }

    protected void process(ProxyOptions proxyOptions, Object proxyOptionsBean) throws BeansException {
        var trimProxyOptions = new TrimProxyOptions(proxyOptions);
        var proxyResourceLoader = new ProxyResourceLoader(trimProxyOptions, this.applicationContext,
                this.proxyRestOperationsProvider);
        this.openApiManager.parse(trimProxyOptions, proxyResourceLoader, (po, oa) -> buildProxyClient(po, oa))
            .registerPathMappings(proxyOptionsBean);
    }

    protected ProxyClient buildProxyClient(ProxyOptions proxyOptions, OpenAPI openApi) {
        var target = getTarget(openApi, proxyOptions);
        var restOperations = this.proxyRestOperationsProvider.getRestOperations(target);
        return new ProxyClient(this.proxyRestOperationsProvider.isRetryEnabled(target), this.proxyObjectMapper,
                restOperations,
                () -> new ProxyHttpServletRequest(this.proxyRequestSupplier.get(), proxyOptions.getPrefix()),
                proxyOptions.getIgnoredRequestHeaders(), proxyOptions.getIgnoredResponseHeaders());
    }

    protected String getTarget(OpenAPI openApi, ProxyOptions proxyOptions) {
        return Optional.ofNullable(proxyOptions.getTarget())
            .filter((target) -> !target.isBlank())
            .orElseGet(() -> Optional.ofNullable(openApi)
                .map((oa) -> oa.getServers().stream().map((server) -> server.getUrl()).findFirst().orElse(null))
                .orElseThrow(() -> new IllegalArgumentException("No target URL found")));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TrimProxyOptions implements ProxyOptions {

        private final ProxyOptions proxyOptions;

        @Override
        public String getSpecification() {
            return trim(this.proxyOptions.getSpecification());
        }

        @Override
        public String getPrefix() {
            return trim(this.proxyOptions.getPrefix());
        }

        @Override
        public String getTarget() {
            return trim(this.proxyOptions.getTarget());
        }

        @Override
        public List<String> getIgnoredRequestHeaders() {
            return Optional.ofNullable(this.proxyOptions.getIgnoredRequestHeaders())
                .map((headers) -> headers.stream().map(TrimProxyOptions::trim).toList())
                .orElse(null);
        }

        @Override
        public List<String> getIgnoredResponseHeaders() {
            return Optional.ofNullable(this.proxyOptions.getIgnoredResponseHeaders())
                .map((headers) -> headers.stream().map(TrimProxyOptions::trim).toList())
                .orElse(null);
        }

        private static String trim(String value) {
            return Optional.ofNullable(value).map((v) -> v.trim()).orElse(null);
        }

    }

}
