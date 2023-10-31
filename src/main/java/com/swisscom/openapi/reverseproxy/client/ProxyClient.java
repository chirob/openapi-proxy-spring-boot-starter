package com.swisscom.openapi.reverseproxy.client;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.openapi.reverseproxy.annotation.Proxy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProxyClient {

	private static final String REQUEST_ENTITY_ATTR_NAME = Proxy.class.getName().concat("_REQUEST_ENTITY");
	private static final String RESPONSE_ENTITY_ATTR_NAME = Proxy.class.getName().concat("_RESPONSE_ENTITY");

	private final boolean retryEnabled;
	private final ObjectMapper objectMapper;
	private final RestOperations restOperations;
	private final Supplier<HttpServletRequest> requestSupplier;
	private final List<String> ignoredRequestHeaders;
	private final List<String> ignoredResponseHeaders;

	public <T, Q> ProxyClient transformRequestEntity(Function<RequestEntity<T>, RequestEntity<Q>> transformer) {
		requestEntity(transformer.apply(requestEntity(null, false)), retryEnabled);
		return this;
	}

	public ProxyClient transformRequestEntityHeaders(Consumer<HttpHeaders> transformer) {
		var requestEntity = requestEntity(null, false);
		requestEntity(
				RequestEntity.method(requestEntity.getMethod(), buildRequestUri().toUriString()).headers(headers -> {
					headers.addAll(requestEntity.getHeaders());
					transformer.accept(headers);
				}).body(requestEntity.getBody()), retryEnabled);
		return this;
	}

	public <T, Q> ProxyClient transformRequestEntityBody(Function<T, Q> transformer) {
		return transformRequestEntityBody(transformer, null);
	}

	public <T> ProxyClient transformRequestEntityBody(Consumer<T> transformer) {
		return transformRequestEntityBody(transformer, null);
	}

	public <T, Q> ProxyClient transformRequestEntityBody(Function<T, Q> transformer, Object requestBodyType) {
		RequestEntity<Object> requestEntity = requestEntity(null, false);
		var requestBody = requestEntity.getBody();
		T convertedRequestBody = convertRequestBody(requestBody, requestBodyType);
		Q transformedRequestBody = transformer.apply(convertedRequestBody);
		if (requestBody != transformedRequestBody) {
			requestEntity(RequestEntity.method(requestEntity.getMethod(), buildRequestUri().toUriString())
					.headers(requestEntity.getHeaders()).body(transformedRequestBody), retryEnabled);
		}
		return this;
	}

	public <T> ProxyClient transformRequestEntityBody(Consumer<T> transformer, Object requestBodyType) {
		Function<T, T> transf = t -> {
			transformer.accept(t);
			return t;
		};
		return transformRequestEntityBody(transf, requestBodyType);
	}

	public <T> T getResponseBody(ParameterizedTypeReference<T> responseType) {
		return exchange(responseType).getBody();
	}

	public <T> T getResponseBody(Class<T> responseType) {
		return exchange(responseType).getBody();
	}

	public <T> ResponseEntity<T> exchange(ParameterizedTypeReference<T> responseBodyType) {
		return exchangeResponse(responseBodyType);
	}

	public <T> ResponseEntity<T> exchange(Class<T> responseBodyType) {
		return exchangeResponse(responseBodyType);
	}

	public <T> ResponseEntity<T> exchange() {
		return exchangeResponse(Resource.class);
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> updateResponse(Object response) {
		if (response instanceof ResponseEntity) {
			return ((ResponseEntity<T>) response);
		} else {
			var responseEntity = responeEntity(null);
			var bodyBuilder = ResponseEntity.status(responseEntity.getStatusCode())
					.headers(responseEntity.getHeaders());
			return (ResponseEntity<T>) Optional.ofNullable(response).map(b -> bodyBuilder.body(b))
					.orElseGet(() -> bodyBuilder.build());
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> exchangeResponse(Object responseBodyType) {
		try {
			return wrapResponseEntity(exchangeResponse(requestEntity(null, retryEnabled), responseBodyType));
		} catch (Exception e) {
			return (ResponseEntity<T>) getErrorResponseEntity(e, responseBodyType);
		}
	}

	protected <T> ResponseEntity<T> wrapResponseEntity(ResponseEntity<T> responseEntity) {
		return responeEntity(ResponseEntity.status(responseEntity.getStatusCode())
				.headers(new ProxyHeaders(responseEntity)).body(responseEntity.getBody()));
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> exchangeResponse(RequestEntity<?> requestEntity, Object responseBodyType) {
		return (ResponseEntity<T>) (responseBodyType instanceof ParameterizedTypeReference
				? restOperations.exchange(requestEntity, (ParameterizedTypeReference<?>) responseBodyType)
				: restOperations.exchange(requestEntity, (Class<?>) responseBodyType));
	}

	protected ResponseEntity<Resource> getErrorResponseEntity(Exception e, Object responseBodyType) {
		var errorStatusCode = (e instanceof RestClientResponseException
				? ((RestClientResponseException) e).getStatusCode()
				: HttpStatus.INTERNAL_SERVER_ERROR).value();
		var errorHeaders = e instanceof HttpClientErrorException ? ((HttpClientErrorException) e).getResponseHeaders()
				: null;
		var errorBodyType = toClass(responseBodyType instanceof ParameterizedTypeReference<?>
				? ((ParameterizedTypeReference<?>) responseBodyType).getType()
				: (Type) responseBodyType);
		if (Resource.class.isAssignableFrom(errorBodyType)) {
			var errorBody = new ByteArrayResource(e instanceof RestClientResponseException
					? ((RestClientResponseException) e).getResponseBodyAsByteArray()
					: e.toString().getBytes(StandardCharsets.UTF_8));
			return responeEntity(ResponseEntity.status(errorStatusCode).headers(
					Optional.ofNullable(errorHeaders).map(headers -> new ProxyHeaders(errorHeaders)).orElse(null))
					.body(errorBody));
		}
		throw e instanceof RuntimeException ? ((RuntimeException) e) : new RestClientException(e.getMessage(), e);
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> responeEntity(ResponseEntity<T> responseEntity) {
		if (responseEntity != null) {
			getRequest().setAttribute(RESPONSE_ENTITY_ATTR_NAME, responseEntity);
		}
		return (ResponseEntity<T>) getRequest().getAttribute(RESPONSE_ENTITY_ATTR_NAME);
	}

	@SuppressWarnings("unchecked")
	protected <T> T convertRequestBody(Object requestBody, Object requestBodyType) {
		try {
			return requestBody instanceof Resource
					? requestBodyType instanceof TypeReference
							? objectMapper.readValue(((Resource) requestBody).getInputStream(),
									(TypeReference<T>) requestBodyType)
							: requestBodyType instanceof Class
									? objectMapper.readValue(((Resource) requestBody).getInputStream(),
											(Class<T>) requestBodyType)
									: (T) requestBody
					: (T) requestBody;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> RequestEntity<T> requestEntity(RequestEntity<T> requestEntity, boolean proxyRequestBody) {
		var reqEntity = Optional.ofNullable(requestEntity)
				.orElseGet(() -> Optional
						.ofNullable((RequestEntity<T>) getRequest().getAttribute(REQUEST_ENTITY_ATTR_NAME))
						.orElseGet(() -> (RequestEntity<T>) buildRequestEntity(proxyRequestBody)));
		getRequest().setAttribute(REQUEST_ENTITY_ATTR_NAME, reqEntity);
		return reqEntity;
	}

	protected RequestEntity<Object> buildRequestEntity(boolean proxyRequestBody) {
		var uri = buildRequestUri();
		var method = buildRequestMethod();
		var headers = buildRequestHeaders();
		var body = buildRequestBody(uri, proxyRequestBody);
		return RequestEntity.method(method, uri.toUriString()).headers(headers).body(body);
	}

	protected HttpMethod buildRequestMethod() {
		return HttpMethod.valueOf(getRequest().getMethod());
	}

	protected UriComponents buildRequestUri() {
		var uriBuilder = UriComponentsBuilder.fromPath(getRequest().getRequestURI());
		Optional.ofNullable(getRequest().getQueryString())
				.ifPresent(qs -> Arrays.stream(qs.split("&")).map(p -> p.split("=")[0])
						.forEach(pn -> uriBuilder.queryParam(pn, (Object[]) getRequest().getParameterValues(pn))));
		return uriBuilder.build().encode();
	}

	protected Object buildRequestBody(UriComponents requestUri, boolean proxyRequestBody) {
		var request = getRequest();
		var contentType = Optional.ofNullable(request.getContentType()).map(ct -> MediaType.valueOf(ct)).orElse(null);
		if (MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(contentType)) {
			@SuppressWarnings("unchecked")
			var requestBodyMap = new LinkedMultiValueMap<String, String>(Map.ofEntries(request.getParameterMap()
					.entrySet().stream().map(e -> Map.entry(e.getKey(), List.of(e.getValue()))).toArray(Entry[]::new)));
			requestBodyMap.keySet().removeAll(requestUri.getQueryParams().keySet());
			return requestBodyMap;
		} else {
			try {
				return proxyRequestBody ? new ProxyResource(request.getInputStream())
						: new InputStreamResource(request.getInputStream());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	protected void cleanRequestAttributes() {
		getRequest().removeAttribute(REQUEST_ENTITY_ATTR_NAME);
		getRequest().removeAttribute(RESPONSE_ENTITY_ATTR_NAME);
	}

	protected HttpHeaders buildRequestHeaders() {
		return new ProxyHeaders(getRequest());
	}

	protected HttpServletRequest getRequest() {
		return requestSupplier.get();
	}

	@SuppressWarnings("serial")
	private class ProxyHeaders extends HttpHeaders {
		private final List<String> ignoredRequestHeaders = new ArrayList<>(List.of(HttpHeaders.HOST));
		private final List<String> ignoredResponseHeaders = new ArrayList<>(
				List.of(HttpHeaders.CONNECTION, HttpHeaders.TRANSFER_ENCODING));

		{
			ignoredRequestHeaders.addAll(ProxyClient.this.ignoredRequestHeaders);
			ignoredResponseHeaders.addAll(ProxyClient.this.ignoredResponseHeaders);
		}

		private ProxyHeaders(HttpServletRequest request) {
			Collections.list(request.getHeaderNames()).stream()
					.forEach(hn -> addAll(hn, Collections.list(request.getHeaders(hn))));
			ignoredRequestHeaders.forEach(name -> remove(name));
		}

		private ProxyHeaders(ResponseEntity<?> responseEntity) {
			this(responseEntity.getHeaders());
		}

		private ProxyHeaders(HttpHeaders responseHeaders) {
			addAll(responseHeaders);
			ignoredResponseHeaders.forEach(name -> remove(name));
		}
	}

	private static Class<?> toClass(Type type) {
		return (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
	}

}
