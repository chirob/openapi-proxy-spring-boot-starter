package com.swisscom.openapi.reverseproxy.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class ProxyHttpServletRequest extends HttpServletRequestWrapper {

	private String requestUri;

	public ProxyHttpServletRequest(HttpServletRequest request, String prefix) {
		super(request);
		requestUri = super.getRequestURI().substring(prefix.isBlank() ? 0 : prefix.length() + 1);
	}

	@Override
	public String getRequestURI() {
		return requestUri;
	}

}
