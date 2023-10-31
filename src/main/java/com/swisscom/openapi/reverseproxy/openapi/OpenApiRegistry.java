package com.swisscom.openapi.reverseproxy.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenApiRegistry {

	public static interface Listener {
		void onOpenApiRegistration(String prefix, OpenAPI openApi);
	}

	private List<Listener> listeners = new ArrayList<>();
	private Map<String, List<OpenAPI>> registry = new HashMap<>();

	public OpenAPI get(String prefix) {
		return Optional.ofNullable(prefix).map(pfx -> pfx.trim()).map(registry::get)
				.filter(openApis -> openApis.size() == 1).map(openApis -> openApis.get(0))
				.orElseThrow(() -> new IllegalArgumentException("No OpenAPI instance found for prefix " + prefix));
	}

	public List<OpenAPI> getUnprefixed() {
		return registry.get("");
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	protected void add(String prefix, OpenAPI openApi) {
		var key = Optional.ofNullable(prefix).map(pfx -> pfx.trim()).orElse("");
		registry.computeIfAbsent(key, k -> new ArrayList<OpenAPI>()).add(openApi);
		listeners.forEach(listener -> listener.onOpenApiRegistration(prefix, openApi));
	}

}
