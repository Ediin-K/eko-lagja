package com.ekolagja.eko_lagja.notices.domain;

import java.time.Instant;
import java.util.Objects;

public class Notification {

	private final Long id;
	private final Long recipientId;
	private final NotificationType type;
	private final Long itemId;
	private final Instant createdAt;
	private Instant readAt;

	private Notification(Long id, Long recipientId, NotificationType type, Long itemId,
			Instant createdAt, Instant readAt) {
		this.id = id;
		this.recipientId = Objects.requireNonNull(recipientId, "recipientId");
		this.type = Objects.requireNonNull(type, "type");
		this.itemId = Objects.requireNonNull(itemId, "itemId");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.readAt = readAt;
	}

	public static Notification create(Long recipientId, NotificationType type, Long itemId, Instant now) {
		return new Notification(null, recipientId, type, itemId, now, null);
	}

	public static Notification restore(Long id, Long recipientId, NotificationType type, Long itemId,
			Instant createdAt, Instant readAt) {
		Objects.requireNonNull(id, "id");
		return new Notification(id, recipientId, type, itemId, createdAt, readAt);
	}

	public void markRead(Instant now) {
		Objects.requireNonNull(now, "now");
		if (readAt == null) {
			readAt = now;
		}
	}

	public boolean isRead() {
		return readAt != null;
	}

	public Long getId() {
		return id;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public NotificationType getType() {
		return type;
	}

	public Long getItemId() {
		return itemId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}
}
