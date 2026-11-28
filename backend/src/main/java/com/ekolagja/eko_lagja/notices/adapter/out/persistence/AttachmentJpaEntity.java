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

import com.ekolagja.eko_lagja.notices.domain.FileType;
import com.ekolagja.eko_lagja.notices.domain.OwnerType;

@Entity
@Table(name = "attachments",
		indexes = @Index(name = "idx_attachments_owner", columnList = "owner_type, owner_id"))
class AttachmentJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;

	@Enumerated(EnumType.STRING)
	@Column(name = "file_type", nullable = false, length = 10)
	private FileType fileType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", nullable = false, length = 20)
	private OwnerType ownerType;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

	@Column(name = "storage_key", nullable = false, unique = true, length = 64)
	private String storageKey;

	protected AttachmentJpaEntity() {
	}

	AttachmentJpaEntity(String fileName, FileType fileType, long sizeBytes, Instant uploadedAt,
			OwnerType ownerType, Long ownerId, String storageKey) {
		this.fileName = fileName;
		this.fileType = fileType;
		this.sizeBytes = sizeBytes;
		this.uploadedAt = uploadedAt;
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.storageKey = storageKey;
	}

	Long getId() {
		return id;
	}

	String getFileName() {
		return fileName;
	}

	FileType getFileType() {
		return fileType;
	}

	long getSizeBytes() {
		return sizeBytes;
	}

	Instant getUploadedAt() {
		return uploadedAt;
	}

	OwnerType getOwnerType() {
		return ownerType;
	}

	Long getOwnerId() {
		return ownerId;
	}

	String getStorageKey() {
		return storageKey;
	}
}
