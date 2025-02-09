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

import org.springframework.http.ResponseEntity;

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
		return ResponseEntity.ok(this.yaml ? Yaml.pretty(this.openApi) : Json.pretty(this.openApi));
	}

}
