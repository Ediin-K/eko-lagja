package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.ekolagja.eko_lagja.notices.application.port.out.Notifications;
import com.ekolagja.eko_lagja.notices.domain.Notification;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

@DataJpaTest
@Import(NotificationPersistenceAdapter.class)
class NotificationPersistenceAdapterTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final Long RESIDENT_ID = 21L;
	private static final Long NEIGHBOUR_ID = 22L;

	@Autowired
	private Notifications notifications;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void savedNotificationsAreFoundByRecipientNewestFirst() {
		notifications.saveAll(List.of(
				Notification.create(RESIDENT_ID, NotificationType.NEW_NOTICE, 4L, NOW.minusSeconds(3600)),
				Notification.create(NEIGHBOUR_ID, NotificationType.NEW_NOTICE, 4L, NOW),
				Notification.create(RESIDENT_ID, NotificationType.COMPLAINT_STATUS_CHANGED, 42L, NOW)));
		clearCache();

		List<Notification> mine = notifications.findByRecipient(RESIDENT_ID);

		assertThat(mine).extracting(Notification::getItemId).containsExactly(42L, 4L);
		assertThat(mine).allSatisfy(notification -> {
			assertThat(notification.getId()).isNotNull();
			assertThat(notification.getRecipientId()).isEqualTo(RESIDENT_ID);
			assertThat(notification.isRead()).isFalse();
		});
		assertThat(mine.get(0).getType()).isEqualTo(NotificationType.COMPLAINT_STATUS_CHANGED);
		assertThat(mine.get(0).getCreatedAt()).isEqualTo(NOW);
	}

	@Test
	void readTimeIsSaved() {
		notifications.saveAll(List.of(Notification.create(RESIDENT_ID, NotificationType.NEW_NOTICE, 4L, NOW)));
		clearCache();
		Notification notification = notifications.findByRecipient(RESIDENT_ID).get(0);

		notification.markRead(NOW.plusSeconds(60));
		notifications.saveAll(List.of(notification));
		clearCache();

		assertThat(notifications.findById(notification.getId()).orElseThrow().getReadAt())
				.isEqualTo(NOW.plusSeconds(60));
	}

	private void clearCache() {
		entityManager.flush();
		entityManager.clear();
	}
}
