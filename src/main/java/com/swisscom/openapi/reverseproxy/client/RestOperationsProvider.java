package com.swisscom.openapi.reverseproxy.client;

import org.springframework.web.client.RestOperations;

public interface RestOperationsProvider {

	RestOperations getRestOperations(String target);

	default boolean isRetryEnabled(String target) {
		return false;
	}

}
