package com.ekolagja.eko_lagja.notices.domain;

public class NoticeNotActiveException extends RuntimeException {

	private final NoticeStatus status;

	public NoticeNotActiveException(NoticeStatus status) {
		super("Notice is not active: " + status);
		this.status = status;
	}

	public NoticeStatus getStatus() {
		return status;
	}
}
