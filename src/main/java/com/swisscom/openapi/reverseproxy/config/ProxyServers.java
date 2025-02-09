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

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.servers.Server;

@SuppressWarnings("serial")
public class ProxyServers extends ArrayList<Server> {

	public ProxyServers(List<String> urls) {
		for (String url : urls) {
			var server = new Server();
			add(server);
			server.setUrl(url);
		}
	}

}
