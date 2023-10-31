package com.swisscom.openapi.reverseproxy.config;

import java.util.List;

public interface ProxyOptions {

	String getSpecification();

	String getPrefix();

	String getTarget();

	List<String> getIgnoredRequestHeaders();

	List<String> getIgnoredResponseHeaders();

}
