package com.ekolagja.eko_lagja.notices.application.port.in;

import java.time.Instant;

import com.ekolagja.eko_lagja.notices.domain.Notification;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

public record NotificationView(
		Long id,
		NotificationType type,
		Long itemId,
		Instant createdAt,
		boolean read) {

	public static NotificationView of(Notification notification) {
		return new NotificationView(notification.getId(), notification.getType(),
				notification.getItemId(), notification.getCreatedAt(), notification.isRead());
	}
}
