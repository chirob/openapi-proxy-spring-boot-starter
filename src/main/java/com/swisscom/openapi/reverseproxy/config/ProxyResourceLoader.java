package com.swisscom.openapi.reverseproxy.config;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.swisscom.openapi.reverseproxy.client.RestOperationsProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProxyResourceLoader implements ResourceLoader {

	private final ProxyOptions proxyOptions;
	private final ResourceLoader resourceLoader;
	private final RestOperationsProvider restOperationsProvider;

	@Override
	public Resource getResource(String location) {
		return Optional.ofNullable(URI.create(location))
				.filter(uri -> List.of("http", "https").contains(Optional.ofNullable(uri.getScheme()).orElse("")))
				.map(uri -> Optional.ofNullable(getRestResource(
						uri.getScheme().concat("://").concat(uri.getAuthority()),
						uri.getPath().concat(
								Optional.ofNullable(uri.getQuery()).map(query -> "?".concat(query)).orElse("")))))
				.orElseGet(() -> Optional.ofNullable(getRestResource(proxyOptions.getTarget(), location)))
				.orElseGet(() -> Optional.of(resourceLoader.getResource(location)).filter(resource -> resource.exists())
						.orElseGet(() -> new ByteArrayResource(location.getBytes(StandardCharsets.UTF_8))));
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	protected Resource getRestResource(String baseUrl, String path) {
		try {
			return restOperationsProvider.getRestOperations(baseUrl).getForObject(path, Resource.class);
		} catch (Exception e) {
			return null;
		}
	}
}
