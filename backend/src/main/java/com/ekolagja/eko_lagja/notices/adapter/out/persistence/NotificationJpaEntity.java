package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.ekolagja.eko_lagja.notices.domain.NotificationType;

@Entity
@Table(name = "notifications",
		indexes = @Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"))
class NotificationJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 40)
	private NotificationType type;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	protected NotificationJpaEntity() {
	}

	NotificationJpaEntity(Long recipientId, NotificationType type, Long itemId, Instant createdAt, Instant readAt) {
		this.recipientId = recipientId;
		this.type = type;
		this.itemId = itemId;
		this.createdAt = createdAt;
		this.readAt = readAt;
	}

	void setReadAt(Instant readAt) {
		this.readAt = readAt;
	}

	Long getId() {
		return id;
	}

	Long getRecipientId() {
		return recipientId;
	}

	NotificationType getType() {
		return type;
	}

	Long getItemId() {
		return itemId;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getReadAt() {
		return readAt;
	}
}
