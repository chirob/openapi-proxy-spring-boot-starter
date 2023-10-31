package com.swisscom.openapi.reverseproxy.client;

import java.util.Optional;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestOperations;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProxyRestOperationProvider implements RestOperationsProvider {

	private final Optional<RestTemplateBuilder> restTemplateBuilder;

	@Override
	public RestOperations getRestOperations(String target) {
		return restTemplateBuilder.orElseGet(() -> new RestTemplateBuilder()).rootUri(target).build();
	}

}
