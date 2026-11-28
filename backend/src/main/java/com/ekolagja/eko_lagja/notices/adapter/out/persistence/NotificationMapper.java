package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import com.ekolagja.eko_lagja.notices.domain.Notification;

final class NotificationMapper {

	private NotificationMapper() {
	}

	static Notification toDomain(NotificationJpaEntity entity) {
		return Notification.restore(entity.getId(), entity.getRecipientId(), entity.getType(),
				entity.getItemId(), entity.getCreatedAt(), entity.getReadAt());
	}

	static NotificationJpaEntity toNewEntity(Notification notification) {
		return new NotificationJpaEntity(notification.getRecipientId(), notification.getType(),
				notification.getItemId(), notification.getCreatedAt(), notification.getReadAt());
	}
}
