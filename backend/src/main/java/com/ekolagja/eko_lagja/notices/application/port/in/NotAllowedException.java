package com.ekolagja.eko_lagja.notices.application.port.in;

public class NotAllowedException extends RuntimeException {

	public NotAllowedException(String message) {
		super(message);
	}
}
