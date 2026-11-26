package com.ekolagja.eko_lagja.notices.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class NotificationTest {

	private static final Long RESIDENT_ID = 7L;
	private static final Long COMPLAINT_ID = 42L;
	private static final Instant CREATED_AT = Instant.parse("2026-11-23T09:00:00Z");
	private static final Instant OPENED_AT = Instant.parse("2026-11-23T10:15:00Z");
	private static final Instant OPENED_AGAIN_AT = Instant.parse("2026-11-24T08:00:00Z");

	@Test
	void newNotificationIsUnreadAndKeepsItsData() {
		Notification notification = newNotification();

		assertThat(notification.getId()).isNull();
		assertThat(notification.getRecipientId()).isEqualTo(RESIDENT_ID);
		assertThat(notification.getType()).isEqualTo(NotificationType.COMPLAINT_STATUS_CHANGED);
		assertThat(notification.getItemId()).isEqualTo(COMPLAINT_ID);
		assertThat(notification.getCreatedAt()).isEqualTo(CREATED_AT);
		assertThat(notification.getReadAt()).isNull();
		assertThat(notification.isRead()).isFalse();
	}

	@Test
	void recipientTypeItemAndTimeAreRequired() {
		assertThatNullPointerException().isThrownBy(() -> Notification.create(
				null, NotificationType.NEW_NOTICE, COMPLAINT_ID, CREATED_AT));
		assertThatNullPointerException().isThrownBy(() -> Notification.create(
				RESIDENT_ID, null, COMPLAINT_ID, CREATED_AT));
		assertThatNullPointerException().isThrownBy(() -> Notification.create(
				RESIDENT_ID, NotificationType.NEW_NOTICE, null, CREATED_AT));
		assertThatNullPointerException().isThrownBy(() -> Notification.create(
				RESIDENT_ID, NotificationType.NEW_NOTICE, COMPLAINT_ID, null));
	}

	@Test
	void openingANotificationMarksItRead() {
		Notification notification = newNotification();

		notification.markRead(OPENED_AT);

		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isEqualTo(OPENED_AT);
	}

	@Test
	void openingItAgainKeepsTheFirstReadTime() {
		Notification notification = newNotification();
		notification.markRead(OPENED_AT);

		notification.markRead(OPENED_AGAIN_AT);

		assertThat(notification.getReadAt()).isEqualTo(OPENED_AT);
	}

	@Test
	void readTimeIsRequired() {
		Notification notification = newNotification();

		assertThatNullPointerException().isThrownBy(() -> notification.markRead(null));
		assertThat(notification.isRead()).isFalse();
	}

	@Test
	void restoredNotificationKeepsItsSavedState() {
		Notification notification = Notification.restore(9L, RESIDENT_ID, NotificationType.NEW_NOTICE,
				COMPLAINT_ID, CREATED_AT, OPENED_AT);

		assertThat(notification.getId()).isEqualTo(9L);
		assertThat(notification.getType()).isEqualTo(NotificationType.NEW_NOTICE);
		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isEqualTo(OPENED_AT);
	}

	private static Notification newNotification() {
		return Notification.create(RESIDENT_ID, NotificationType.COMPLAINT_STATUS_CHANGED, COMPLAINT_ID, CREATED_AT);
	}
}
