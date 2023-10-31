package com.swisscom.openapi.reverseproxy.openapi;

import io.swagger.v3.oas.models.OpenAPI;

public interface OpenApiProvider {

	OpenAPI getOpenAPI();
	
}
