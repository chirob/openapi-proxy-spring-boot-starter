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

package com.swisscom.openapi.reverseproxy.openapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import com.swisscom.openapi.reverseproxy.client.ProxyClient;
import com.swisscom.openapi.reverseproxy.client.ProxyClientMethodInterceptor;
import com.swisscom.openapi.reverseproxy.client.ProxySwaggerSpecMethodInterceptor;
import com.swisscom.openapi.reverseproxy.config.ProxyOptions;
import com.swisscom.openapi.reverseproxy.util.RequestMappingRegistrationHandler;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenApiManager {

	private final RequestMappingRegistrationHandler requestMappingRegistrationHandler;

	private final OpenApiProvider openApiProvider;

	private final OpenApiRegistry openApiRegistry;

	private final List<Server> proxyServers;

	private ProxyClient proxyClient;

	private ProxyOptions proxyOptions;

	private OpenAPI openApi;

	private static final ParseOptions OPENAPI_PARSE_OPTION = new ParseOptions() {
		{
			setResolve(true);
		}
	};

	public OpenApiManager parse(ProxyOptions proxyOptions, ResourceLoader proxyResourceLoader,
			ProxyClientBuilder proxyClientBuilder) {
		this.proxyOptions = proxyOptions;
		if (Optional.ofNullable(proxyOptions.getSpecification()).filter((spec) -> !spec.isBlank()).isPresent()) {
			this.openApi = parseSpecification(proxyResourceLoader);
			this.openApiRegistry.add(proxyOptions.getPrefix(), this.openApi);
			var rootOpenApi = this.openApiProvider.getOpenAPI();
			var currentApi = Optional.ofNullable(rootOpenApi)
				.orElseGet(() -> Optional.ofNullable(this.proxyOptions.getPrefix())
					.filter((prefix) -> !prefix.isBlank())
					.map((prefix) -> this.openApi)
					.orElse(null));
			if (this.openApi == currentApi) {
				if (rootOpenApi == null) {
					registerDocsMapping(this.proxyOptions.getPrefix(), false);
					registerDocsMapping(this.proxyOptions.getPrefix(), true);
				}
			}
			else {
				mergeOpenApi(currentApi, this.openApi);
			}
		}
		else {
			this.openApi = null;
		}
		this.proxyClient = proxyClientBuilder.build(proxyOptions, this.openApi);
		return this;
	}

	public OpenApiManager registerPathMappings(Object proxyOptionsBean) {
		if (this.openApi == null) {
			registerOperationMapping(proxyOptionsBean, "/".concat(this.proxyOptions.getPrefix()).concat("/**"), null,
					RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
					RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT, RequestMethod.TRACE);
		}
		else {
			for (var pathEntry : this.openApi.getPaths().entrySet()) {
				for (var operationEntry : pathEntry.getValue().readOperationsMap().entrySet()) {
					var requestMethod = RequestMethod.resolve(HttpMethod.valueOf(operationEntry.getKey().toString()));
					registerOperationMapping(proxyOptionsBean, pathEntry.getKey(), operationEntry.getValue(),
							requestMethod);
				}
				this.openApi.setServers(this.proxyServers);
			}
		}
		return this;
	}

	protected void registerOperationMapping(Object proxyOptionsBean, String path, Operation operation,
			RequestMethod... requestMethods) {
		var methodInterceptor = new ProxyClientMethodInterceptor(proxyOptionsBean, path, List.of(requestMethods),
				this.proxyClient);
		this.requestMappingRegistrationHandler.registerMapping(RequestMappingInfo.paths(path)
			.methods(requestMethods)
			.consumes(Optional.ofNullable(operation)
				.map((op) -> op.getRequestBody())
				.map((reqBody) -> reqBody.getContent())
				.map((content) -> content.keySet())
				.map((set) -> set.toArray(String[]::new))
				.orElseGet(() -> new String[0]))
			.produces(Optional.ofNullable(operation)
				.map((op) -> op.getResponses())
				.map((apiResps) -> apiResps.values()
					.stream()
					.filter((apiResp) -> apiResp.getContent() != null)
					.map((apiResp) -> apiResp.getContent())
					.flatMap((content) -> content.keySet().stream())
					.toArray(String[]::new))
				.orElseGet(() -> new String[0])), methodInterceptor);
	}

	protected void registerDocsMapping(String pathPrefix, boolean yaml) {
		var suffix = yaml ? ".yaml" : ".json";
		var contentType = yaml ? "text/plain" : "application/json";
		Optional.ofNullable(pathPrefix)
			.map((prefix) -> "/".concat(prefix).concat("/api-docs").concat(suffix))
			.ifPresent((path) -> Optional.of(new ProxySwaggerSpecMethodInterceptor(this.openApi, false))
				.ifPresent((methodInterceptor) -> this.requestMappingRegistrationHandler.registerMapping(
						RequestMappingInfo.paths(path).methods(RequestMethod.GET).produces(contentType),
						methodInterceptor)));
	}

	protected OpenAPI parseSpecification(ResourceLoader proxyResourceLoader) {
		try {
			var specification = proxyResourceLoader.getResource(this.proxyOptions.getSpecification());
			var openApi = new OpenAPIParser()
				.readContents(IOUtils.toString(specification.getInputStream(), StandardCharsets.UTF_8), null,
						OPENAPI_PARSE_OPTION)
				.getOpenAPI();
			if (openApi == null) {
				throw new FatalBeanException("Unparseable Swagger specification: " + specification);
			}
			var proxyPaths = new Paths();
			openApi.getPaths()
				.forEach((path, pathItem) -> proxyPaths
					.put((this.proxyOptions.getPrefix().isBlank() ? "" : "/".concat(this.proxyOptions.getPrefix()))
						.concat(path), pathItem));
			openApi.setPaths(proxyPaths);
			return openApi;
		}
		catch (IOException ex) {
			throw new FatalBeanException(ex.getMessage(), ex);
		}
	}

	protected void mergeOpenApi(OpenAPI oa1, OpenAPI oa2) {
		if (oa1.getPaths() != null) {
			var copyPaths = new ArrayList<>(oa1.getPaths().keySet());
			copyPaths.retainAll(oa2.getPaths().keySet());
			if (!copyPaths.isEmpty()) {
				throw new IllegalArgumentException("Found duplicated paths: " + oa1.getPaths().keySet());
			}
		}

		if (oa1.getComponents() != null && oa2.getComponents() != null) {
			oa1.getComponents()
				.examples(mergeMap(oa1.getComponents().getExamples(), oa2.getComponents().getExamples()))
				.extensions(mergeMap(oa1.getComponents().getExtensions(), oa2.getComponents().getExtensions()))
				.headers(mergeMap(oa1.getComponents().getHeaders(), oa2.getComponents().getHeaders()))
				.links(mergeMap(oa1.getComponents().getLinks(), oa2.getComponents().getLinks()))
				.parameters(mergeMap(oa1.getComponents().getParameters(), oa2.getComponents().getParameters()))
				.pathItems(mergeMap(oa1.getComponents().getPathItems(), oa2.getComponents().getPathItems()))
				.requestBodies(mergeMap(oa1.getComponents().getRequestBodies(), oa2.getComponents().getRequestBodies()))
				.responses(mergeMap(oa1.getComponents().getResponses(), oa2.getComponents().getResponses()))
				.schemas(mergeMap(oa1.getComponents().getSchemas(), oa2.getComponents().getSchemas()))
				.securitySchemes(
						mergeMap(oa1.getComponents().getSecuritySchemes(), oa2.getComponents().getSecuritySchemes()));
		}

		oa1.security(mergeList(oa1.getSecurity(), oa2.getSecurity()))
			.tags(mergeList(oa1.getTags(), oa2.getTags()))
			.extensions(mergeMap(oa1.getExtensions(), oa2.getExtensions()))
			.paths((Paths) mergeMap(oa1.getPaths(), oa2.getPaths()))
			.components(Optional.ofNullable(oa1.getComponents()).orElse(oa2.getComponents()));
	}

	protected <T> List<T> mergeList(List<T> list1, List<T> list2) {
		Optional.ofNullable(list1).ifPresent((l1) -> Optional.ofNullable(list2).ifPresent(l1::addAll));
		return Optional.ofNullable(list1).orElse(list2);
	}

	protected <K, V> Map<K, V> mergeMap(Map<K, V> map1, Map<K, V> map2) {
		Optional.ofNullable(map1).ifPresent((m1) -> Optional.ofNullable(map2).ifPresent(m1::putAll));
		return Optional.ofNullable(map1).orElse(map2);
	}

	public interface ProxyClientBuilder {

		ProxyClient build(ProxyOptions proxyOptions, OpenAPI openApi);

	}

}
