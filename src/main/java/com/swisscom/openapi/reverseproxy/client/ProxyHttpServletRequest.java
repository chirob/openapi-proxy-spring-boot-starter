/*
 * Copyright 2024-2099 Swisscom (Schweiz) AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swisscom.openapi.reverseproxy.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class ProxyHttpServletRequest extends HttpServletRequestWrapper {

	private String requestUri;

	public ProxyHttpServletRequest(HttpServletRequest request, String prefix) {
		super(request);
		this.requestUri = super.getRequestURI().substring(prefix.isBlank() ? 0 : prefix.length() + 1);
	}

	@Override
	public String getRequestURI() {
		return this.requestUri;
	}

}
