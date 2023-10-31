package com.swisscom.openapi.reverseproxy.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MainTest {

	RestTemplate restTemplate = new RestTemplate();

	@Test
	public void testGetCats() {
		Object response = restTemplate.getForObject("http://localhost:8080/test-proxy/cats", Object.class);
		System.out.println(response);
	}

}