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

package com.swisscom.openapi.reverseproxy.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MainTests {

	RestTemplate restTemplate = new RestTemplate();

	@SuppressWarnings("unchecked")
	@Test
	void testGetCats() throws JsonProcessingException {
		List<Object> response = this.restTemplate.getForObject("http://localhost:8080/cats", List.class);
		log.info("response: {}", response);
		List<Object> proxResponse = this.restTemplate.getForObject("http://localhost:8080/test-proxy/cats", List.class);
		log.info("proxResponse: {}", proxResponse);
		assertEquals(proxResponse.size(), response.size() - 1);
	}

}
