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
			catStore = new ObjectMapper().readValue(getClass().getResourceAsStream("/cats.json"),
					new TypeReference<List<Cat>>() {
					});
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@GetMapping("/cats")
	public List<Cat> getCats() {
		return catStore;
	}

	@PostMapping("/cats")
	public void postCats(@RequestBody List<Cat> cats) {
		catStore.addAll(cats);
	}

}
