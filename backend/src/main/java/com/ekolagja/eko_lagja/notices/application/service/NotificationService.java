package com.ekolagja.eko_lagja.notices.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.ekolagja.eko_lagja.notices.application.port.in.CreateNotificationUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.ListMyNotificationsUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.MarkNotificationReadUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.MyNotifications;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotificationNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotificationView;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.Notifications;
import com.ekolagja.eko_lagja.notices.domain.Notification;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

public class NotificationService implements CreateNotificationUseCase,
		ListMyNotificationsUseCase, MarkNotificationReadUseCase {

	private final Notifications notifications;
	private final CurrentUser currentUser;
	private final Clock clock;

	public NotificationService(Notifications notifications, CurrentUser currentUser, Clock clock) {
		this.notifications = notifications;
		this.currentUser = currentUser;
		this.clock = clock;
	}

	@Override
	public void create(NotificationType type, Set<Long> recipientIds, Long itemId) {
		if (recipientIds.isEmpty()) {
			return;
		}
		Instant now = Instant.now(clock);
		notifications.saveAll(recipientIds.stream()
				.map(recipientId -> Notification.create(recipientId, type, itemId, now))
				.toList());
	}

	@Override
	public MyNotifications listMine() {
		Long userId = requireLoggedIn();
		List<NotificationView> items = notifications.findByRecipient(userId).stream()
				.filter(notification -> notification.getRecipientId().equals(userId))
				.sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
				.map(NotificationView::of)
				.toList();
		long unread = items.stream().filter(item -> !item.read()).count();
		return new MyNotifications(items, unread);
	}

	@Override
	public void markRead(Long notificationId) {
		Long userId = requireLoggedIn();
		Notification notification = notifications.findById(notificationId)
				.filter(found -> found.getRecipientId().equals(userId))
				.orElseThrow(() -> new NotificationNotFoundException(notificationId));
		notification.markRead(Instant.now(clock));
		notifications.saveAll(List.of(notification));
	}

	private Long requireLoggedIn() {
		return currentUser.id().orElseThrow(() -> new NotAllowedException("no logged-in user"));
	}
}
