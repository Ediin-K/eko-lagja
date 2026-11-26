package com.ekolagja.eko_lagja.notices.application.port.in;

import java.util.Set;

import com.ekolagja.eko_lagja.notices.domain.NotificationType;

public interface CreateNotificationUseCase {

	void create(NotificationType type, Set<Long> recipientIds, Long itemId);
}
