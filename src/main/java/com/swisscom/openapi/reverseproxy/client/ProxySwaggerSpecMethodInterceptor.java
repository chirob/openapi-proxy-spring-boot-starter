package com.swisscom.openapi.reverseproxy.client;

import org.springframework.http.ResponseEntity;

import com.google.common.base.Optional;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProxySwaggerSpecMethodInterceptor extends ProxyMethodInterceptor<ResponseEntity<String>> {

	private final OpenAPI openApi;
	private final boolean yaml;

	@Override
	public ResponseEntity<String> invoke() {
		return Optional.of(yaml ? Yaml.pretty(openApi) : Json.pretty(openApi))
				.transform(spec -> ResponseEntity.ok(spec)).get();
	}
}
