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

package com.swisscom.openapi.reverseproxy;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.openapi.reverseproxy.openapi.OpenApiProvider;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;

@ComponentScan
@AutoConfiguration
public class BootConfiguration {

    @ConditionalOnMissingBean(name = "proxyObjectMapper")
    @Bean
    public ObjectMapper proxyObjectMapper() {
        return new ObjectMapper();
    }

    @ConditionalOnMissingBean({ OpenAPI.class, OpenApiProvider.class })
    @Bean
    public OpenAPI proxyOpenApi() {
        return new OpenAPI();
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

}
