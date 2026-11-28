package com.ekolagja.eko_lagja.notices.application.port.in;

public class NoticeNotFoundException extends RuntimeException {

	private final Long noticeId;

	public NoticeNotFoundException(Long noticeId) {
		super("Notice not found: " + noticeId);
		this.noticeId = noticeId;
	}

	public Long getNoticeId() {
		return noticeId;
	}
}
