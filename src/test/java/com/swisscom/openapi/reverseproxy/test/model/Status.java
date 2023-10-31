package com.swisscom.openapi.reverseproxy.test.model;

import lombok.Data;

@Data
public class Status {

	private boolean verified;
	private int sentCount;

	@Override
	public String toString() {
		return "Status [verified=" + verified + ", sentCount=" + sentCount + "]";
	}

}
