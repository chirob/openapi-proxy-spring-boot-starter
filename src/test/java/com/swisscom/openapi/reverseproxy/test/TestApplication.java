package com.swisscom.openapi.reverseproxy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.swisscom.openapi.reverseproxy.annotation.Proxy;
import com.swisscom.openapi.reverseproxy.annotation.ProxyInterceptor;
import com.swisscom.openapi.reverseproxy.annotation.Proxy.Options;
import com.swisscom.openapi.reverseproxy.client.ProxyClient;
import com.swisscom.openapi.reverseproxy.test.model.Cat;

@TestPropertySource
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Proxy(specification = "classpath:/cats.openapi.json", options = @Options(prefix = "test-proxy"))
	@Bean
	public Object catstoreProxy() {
		return new Object() {
			@ProxyInterceptor(path = "/test-proxy/cats", method = RequestMethod.GET)
			public List<Cat> getCats(ProxyClient proxyClient) {
 				var cats = proxyClient.getResponseBody(new ParameterizedTypeReference<List<Cat>>() {
				});
				cats.removeIf(cat -> cat.getCreatedAt().compareTo(thresholdDate()) < 0);
				return cats;
			}

			@ProxyInterceptor(path = "/test-proxy/cats", method = RequestMethod.POST)
			public void postCats(ProxyClient proxyClient) {
				Consumer<List<Cat>> transformer = cats -> cats.forEach(cat -> cat.setUpdatedAt(new Date()));
				proxyClient.transformRequestEntityBody(transformer, new TypeReference<List<Cat>>() {
				}).exchange();
			}

			private Date thresholdDate() {
				try {
					return new SimpleDateFormat("dd-MM-yyyy").parse("01-02-2018");
				} catch (ParseException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};
	}

}
