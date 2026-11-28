package com.ekolagja.eko_lagja.notices.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ekolagja.eko_lagja.notices.application.port.in.MyNotifications;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotificationNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotificationView;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.Notifications;
import com.ekolagja.eko_lagja.notices.domain.Notification;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

class NotificationServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final Long RESIDENT_ID = 21L;
	private static final Long NEIGHBOUR_ID = 22L;
	private static final Long NOTICE_ID = 4L;

	private final InMemoryNotifications notifications = new InMemoryNotifications();
	private final FakeCurrentUser currentUser = new FakeCurrentUser();
	private final NotificationService service = new NotificationService(notifications, currentUser,
			Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void createMakesOneUnreadNotificationPerRecipient() {
		service.create(NotificationType.NEW_NOTICE, Set.of(RESIDENT_ID, NEIGHBOUR_ID), NOTICE_ID);

		assertThat(notifications.all()).hasSize(2)
				.allSatisfy(notification -> {
					assertThat(notification.getType()).isEqualTo(NotificationType.NEW_NOTICE);
					assertThat(notification.getItemId()).isEqualTo(NOTICE_ID);
					assertThat(notification.getCreatedAt()).isEqualTo(NOW);
					assertThat(notification.isRead()).isFalse();
				})
				.extracting(Notification::getRecipientId)
				.containsExactlyInAnyOrder(RESIDENT_ID, NEIGHBOUR_ID);
	}

	@Test
	void noRecipientsCreatesNothing() {
		service.create(NotificationType.NEW_NOTICE, Set.of(), NOTICE_ID);

		assertThat(notifications.all()).isEmpty();
	}

	@Test
	void residentSeesOnlyOwnNotificationsNewestFirstWithUnreadCount() {
		Long oldRead = notifications.seed(RESIDENT_ID, NOW.minusSeconds(7200), NOW.minusSeconds(3600));
		Long newest = notifications.seed(RESIDENT_ID, NOW.minusSeconds(60), null);
		Long middle = notifications.seed(RESIDENT_ID, NOW.minusSeconds(3600), null);
		notifications.seed(NEIGHBOUR_ID, NOW, null);
		currentUser.logInAs(RESIDENT_ID);

		MyNotifications mine = service.listMine();

		assertThat(mine.items()).extracting(NotificationView::id)
				.containsExactly(newest, middle, oldRead);
		assertThat(mine.unreadCount()).isEqualTo(2);
	}

	@Test
	void visitorCannotListOrMarkRead() {
		Long id = notifications.seed(RESIDENT_ID, NOW, null);
		currentUser.logOut();

		assertThatThrownBy(service::listMine).isInstanceOf(NotAllowedException.class);
		assertThatThrownBy(() -> service.markRead(id)).isInstanceOf(NotAllowedException.class);
		assertThat(notifications.byId(id).isRead()).isFalse();
	}

	@Test
	void openingANotificationMarksItRead() {
		Long id = notifications.seed(RESIDENT_ID, NOW.minusSeconds(60), null);
		currentUser.logInAs(RESIDENT_ID);

		service.markRead(id);

		assertThat(notifications.byId(id).getReadAt()).isEqualTo(NOW);
		assertThat(service.listMine().unreadCount()).isZero();
	}

	@Test
	void openingItAgainKeepsTheFirstReadTime() {
		Instant firstOpened = NOW.minusSeconds(600);
		Long id = notifications.seed(RESIDENT_ID, NOW.minusSeconds(3600), firstOpened);
		currentUser.logInAs(RESIDENT_ID);

		service.markRead(id);

		assertThat(notifications.byId(id).getReadAt()).isEqualTo(firstOpened);
	}

	@Test
	void someoneElsesNotificationLooksLikeItDoesNotExist() {
		Long neighbours = notifications.seed(NEIGHBOUR_ID, NOW, null);
		currentUser.logInAs(RESIDENT_ID);

		assertThatThrownBy(() -> service.markRead(neighbours))
				.isInstanceOf(NotificationNotFoundException.class);
		assertThat(notifications.byId(neighbours).isRead()).isFalse();
	}

	@Test
	void missingNotificationIsNotFound() {
		currentUser.logInAs(RESIDENT_ID);

		assertThatThrownBy(() -> service.markRead(404L))
				.isInstanceOf(NotificationNotFoundException.class);
	}

	private static class FakeCurrentUser implements CurrentUser {

		private Long id;

		void logInAs(Long userId) {
			id = userId;
		}

		void logOut() {
			id = null;
		}

		@Override
		public Optional<Long> id() {
			return Optional.ofNullable(id);
		}

		@Override
		public boolean isOfficial() {
			return false;
		}
	}

	private static class InMemoryNotifications implements Notifications {

		private final Map<Long, Notification> byId = new HashMap<>();
		private long nextId = 1;

		Long seed(Long recipientId, Instant createdAt, Instant readAt) {
			Long id = nextId++;
			byId.put(id, Notification.restore(id, recipientId, NotificationType.NEW_NOTICE, NOTICE_ID,
					createdAt, readAt));
			return id;
		}

		Notification byId(Long id) {
			return byId.get(id);
		}

		List<Notification> all() {
			return List.copyOf(byId.values());
		}

		@Override
		public void saveAll(List<Notification> notifications) {
			for (Notification notification : notifications) {
				Notification stored = notification.getId() != null ? notification
						: Notification.restore(nextId++, notification.getRecipientId(), notification.getType(),
								notification.getItemId(), notification.getCreatedAt(), notification.getReadAt());
				byId.put(stored.getId(), stored);
			}
		}

		@Override
		public Optional<Notification> findById(Long id) {
			return Optional.ofNullable(byId.get(id));
		}

		@Override
		public List<Notification> findByRecipient(Long userId) {
			return byId.values().stream()
					.filter(notification -> notification.getRecipientId().equals(userId))
					.toList();
		}
	}
}
