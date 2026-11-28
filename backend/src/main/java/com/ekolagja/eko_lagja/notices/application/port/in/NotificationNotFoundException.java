package com.ekolagja.eko_lagja.notices.application.port.in;

public class NotificationNotFoundException extends RuntimeException {

	private final Long notificationId;

	public NotificationNotFoundException(Long notificationId) {
		super("Notification not found: " + notificationId);
		this.notificationId = notificationId;
	}

	public Long getNotificationId() {
		return notificationId;
	}
}
