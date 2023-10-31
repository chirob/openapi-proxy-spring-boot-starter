package com.swisscom.openapi.reverseproxy.test.model;

import java.util.Date;

import lombok.Data;

@Data
public class Cat {

	private Status status;
	private String get_id;
	private String type;
	private String user;
	private String text;
	private int get__v;
	private String source;
	private Date createdAt;
	private Date updatedAt;
	private boolean deleted;
	private boolean used;

	@Override
	public String toString() {
		return "Cat [status=" + status + ", get_id=" + get_id + ", type=" + type + ", user=" + user + ", text=" + text
				+ ", get__v=" + get__v + ", source=" + source + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", deleted=" + deleted + ", used=" + used + "]";
	}

}
