package com.swisscom.openapi.reverseproxy.config;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.servers.Server;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class ProxyServers extends ArrayList<Server> {

	public ProxyServers(List<String> urls) {
		for (String url : urls) {
			var server = new Server();
			add(server);
			server.setUrl(url);
		}
	}

}
