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

	public static interface ProxyClientBuilder {
		ProxyClient build(ProxyOptions proxyOptions, OpenAPI openApi);
	}

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
		if (Optional.ofNullable(proxyOptions.getSpecification()).filter(spec -> !spec.isBlank()).isPresent()) {
			openApi = parseSpecification(proxyResourceLoader);
			openApiRegistry.add(proxyOptions.getPrefix(), openApi);
			var rootOpenApi = openApiProvider.getOpenAPI();
			var currentApi = Optional.ofNullable(rootOpenApi)
					.orElseGet(() -> Optional.ofNullable(proxyOptions.getPrefix()).filter(prefix -> !prefix.isBlank())
							.map(prefix -> openApi).orElse(null));
			if (openApi == currentApi) {
				if (rootOpenApi == null) {
					registerDocsMapping(proxyOptions.getPrefix(), false);
					registerDocsMapping(proxyOptions.getPrefix(), true);
				}
			} else {
				mergeOpenApi(currentApi, openApi);
			}
		} else {
			openApi = null;
		}
		proxyClient = proxyClientBuilder.build(proxyOptions, openApi);
		return this;
	}

	public OpenApiManager registerPathMappings(Object proxyOptionsBean) {
		if (openApi == null) {
			registerOperationMapping(proxyOptionsBean, "/".concat(proxyOptions.getPrefix()).concat("/**"), null,
					RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
					RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT, RequestMethod.TRACE);
		} else {
			for (var pathEntry : openApi.getPaths().entrySet()) {
				for (var operationEntry : pathEntry.getValue().readOperationsMap().entrySet()) {
					var requestMethod = RequestMethod.resolve(HttpMethod.valueOf(operationEntry.getKey().toString()));
					registerOperationMapping(proxyOptionsBean, pathEntry.getKey(), operationEntry.getValue(),
							requestMethod);
				}
				openApi.setServers(proxyServers);
			}
		}
		return this;
	}

	protected void registerOperationMapping(Object proxyOptionsBean, String path, Operation operation,
			RequestMethod... requestMethods) {
		var methodInterceptor = new ProxyClientMethodInterceptor(proxyOptionsBean, path, List.of(requestMethods),
				proxyClient);
		requestMappingRegistrationHandler.registerMapping(RequestMappingInfo.paths(path).methods(requestMethods)
				.consumes(Optional.ofNullable(operation).map(op -> op.getRequestBody())
						.map(reqBody -> reqBody.getContent()).map(content -> content.keySet())
						.map(set -> set.toArray(String[]::new)).orElse(new String[0]))
				.produces(Optional.ofNullable(operation).map(op -> op.getResponses())
						.map(apiResps -> apiResps.values().stream().filter(apiResp -> apiResp.getContent() != null)
								.map(apiResp -> apiResp.getContent()).flatMap(content -> content.keySet().stream())
								.toArray(String[]::new))
						.orElse(new String[0])),
				methodInterceptor);
	}

	protected void registerDocsMapping(String pathPrefix, boolean yaml) {
		var suffix = yaml ? ".yaml" : ".json";
		var contentType = yaml ? "text/plain" : "application/json";
		Optional.ofNullable(pathPrefix).map(prefix -> "/".concat(prefix).concat("/api-docs").concat(suffix))
				.ifPresent(path -> Optional.of(new ProxySwaggerSpecMethodInterceptor(openApi, false))
						.ifPresent(methodInterceptor -> requestMappingRegistrationHandler.registerMapping(
								RequestMappingInfo.paths(path).methods(RequestMethod.GET).produces(contentType),
								methodInterceptor)));
	}

	protected OpenAPI parseSpecification(ResourceLoader proxyResourceLoader) {
		try {
			var specification = proxyResourceLoader.getResource(proxyOptions.getSpecification());
			var openApi = new OpenAPIParser()
					.readContents(IOUtils.toString(specification.getInputStream(), StandardCharsets.UTF_8), null,
							OPENAPI_PARSE_OPTION)
					.getOpenAPI();
			if (openApi == null) {
				throw new FatalBeanException("Unparseable Swagger specification: " + specification);
			}
			var proxyPaths = new Paths();
			openApi.getPaths().forEach((path, pathItem) -> proxyPaths.put(
					(proxyOptions.getPrefix().isBlank() ? "" : "/".concat(proxyOptions.getPrefix())).concat(path),
					pathItem));
			openApi.setPaths(proxyPaths);
			return openApi;
		} catch (IOException e) {
			throw new FatalBeanException(e.getMessage(), e);
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
			oa1.getComponents().examples(mergeMap(oa1.getComponents().getExamples(), oa2.getComponents().getExamples()))
					.extensions(mergeMap(oa1.getComponents().getExtensions(), oa2.getComponents().getExtensions()))
					.headers(mergeMap(oa1.getComponents().getHeaders(), oa2.getComponents().getHeaders()))
					.links(mergeMap(oa1.getComponents().getLinks(), oa2.getComponents().getLinks()))
					.parameters(mergeMap(oa1.getComponents().getParameters(), oa2.getComponents().getParameters()))
					.pathItems(mergeMap(oa1.getComponents().getPathItems(), oa2.getComponents().getPathItems()))
					.requestBodies(
							mergeMap(oa1.getComponents().getRequestBodies(), oa2.getComponents().getRequestBodies()))
					.responses(mergeMap(oa1.getComponents().getResponses(), oa2.getComponents().getResponses()))
					.schemas(mergeMap(oa1.getComponents().getSchemas(), oa2.getComponents().getSchemas()))
					.securitySchemes(mergeMap(oa1.getComponents().getSecuritySchemes(),
							oa2.getComponents().getSecuritySchemes()));
		}

		oa1.security(mergeList(oa1.getSecurity(), oa2.getSecurity())).tags(mergeList(oa1.getTags(), oa2.getTags()))
				.extensions(mergeMap(oa1.getExtensions(), oa2.getExtensions()))
				.paths((Paths) mergeMap(oa1.getPaths(), oa2.getPaths()))
				.components(Optional.ofNullable(oa1.getComponents()).orElse(oa2.getComponents()));
	}

	protected <T> List<T> mergeList(List<T> list1, List<T> list2) {
		Optional.ofNullable(list1).ifPresent(l1 -> Optional.ofNullable(list2).ifPresent(l2 -> l1.addAll(l2)));
		return Optional.ofNullable(list1).orElse(list2);
	}

	protected <K, V> Map<K, V> mergeMap(Map<K, V> map1, Map<K, V> map2) {
		Optional.ofNullable(map1).ifPresent(m1 -> Optional.ofNullable(map2).ifPresent(m2 -> m1.putAll(m2)));
		return Optional.ofNullable(map1).orElse(map2);
	}

}
