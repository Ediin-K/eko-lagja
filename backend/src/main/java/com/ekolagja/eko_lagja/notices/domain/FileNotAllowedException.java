package com.ekolagja.eko_lagja.notices.domain;

public class FileNotAllowedException extends RuntimeException {

	public FileNotAllowedException(String message) {
		super(message);
	}
}
