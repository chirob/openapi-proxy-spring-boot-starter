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
			.filter((uri) -> List.of("http", "https").contains(Optional.ofNullable(uri.getScheme()).orElse("")))
			.map((uri) -> Optional.ofNullable(getRestResource(uri.getScheme().concat("://").concat(uri.getAuthority()),
					uri.getPath()
						.concat(Optional.ofNullable(uri.getQuery()).map((query) -> "?".concat(query)).orElse("")))))
			.orElseGet(() -> Optional.ofNullable(getRestResource(this.proxyOptions.getTarget(), location)))
			.orElseGet(() -> Optional.ofNullable(this.resourceLoader.getResource(location))
				.filter((resource) -> resource.exists())
				.orElseGet(() -> new ByteArrayResource(location.getBytes(StandardCharsets.UTF_8))));
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	protected Resource getRestResource(String baseUrl, String path) {
		try {
			return this.restOperationsProvider.getRestOperations(baseUrl).getForObject(path, Resource.class);
		}
		catch (Exception ex) {
			return null;
		}
	}

}
