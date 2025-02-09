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

package com.swisscom.openapi.reverseproxy.test.contoller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.openapi.reverseproxy.test.model.Cat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class TestController {

	private List<Cat> catStore;

	{
		try {
			this.catStore = new ObjectMapper().readValue(getClass().getResourceAsStream("/cats.json"),
					new TypeReference<List<Cat>>() {
					});
		}
		catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@GetMapping("/cats")
	public List<Cat> getCats() {
		return this.catStore;
	}

	@PostMapping("/cats")
	public void postCats(@RequestBody List<Cat> cats) {
		this.catStore.addAll(cats);
	}

}
