package com.ekolagja.eko_lagja.notices.domain;

public class InvalidNoticeException extends RuntimeException {

	private final String field;

	public InvalidNoticeException(String field, String message) {
		super(message);
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
