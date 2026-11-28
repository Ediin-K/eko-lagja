package com.ekolagja.eko_lagja.notices.domain;

import java.time.Instant;
import java.util.Objects;

public class Attachment {

	public static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
	public static final int MAX_FILE_NAME_LENGTH = 255;

	private final Long id;
	private final String fileName;
	private final FileType fileType;
	private final long sizeBytes;
	private final Instant uploadedAt;
	private final AttachmentOwner owner;
	private final String storageKey;

	private Attachment(Long id, String fileName, FileType fileType, long sizeBytes,
			Instant uploadedAt, AttachmentOwner owner, String storageKey) {
		this.id = id;
		this.fileName = fileName;
		this.fileType = fileType;
		this.sizeBytes = sizeBytes;
		this.uploadedAt = uploadedAt;
		this.owner = owner;
		this.storageKey = storageKey;
	}

	public static Attachment create(String fileName, String contentType, long sizeBytes,
			AttachmentOwner owner, String storageKey, Instant now) {
		Objects.requireNonNull(owner, "owner");
		Objects.requireNonNull(now, "now");
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("storageKey must not be blank");
		}
		if (fileName == null || fileName.isBlank()) {
			throw new FileNotAllowedException(FileProblem.NAME_MISSING, "file name is missing");
		}
		if (fileName.length() > MAX_FILE_NAME_LENGTH) {
			throw new FileNotAllowedException(FileProblem.NAME_TOO_LONG,
					"file name must have at most " + MAX_FILE_NAME_LENGTH + " characters");
		}
		FileType fileType = FileType.fromContentType(contentType);
		if (sizeBytes <= 0) {
			throw new FileNotAllowedException(FileProblem.EMPTY, "file is empty");
		}
		if (sizeBytes > MAX_SIZE_BYTES) {
			throw new FileNotAllowedException(FileProblem.TOO_LARGE, "file is larger than 10 MB");
		}
		return new Attachment(null, fileName, fileType, sizeBytes, now, owner, storageKey);
	}

	public static Attachment restore(Long id, String fileName, FileType fileType, long sizeBytes,
			Instant uploadedAt, AttachmentOwner owner, String storageKey) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(fileType, "fileType");
		Objects.requireNonNull(owner, "owner");
		return new Attachment(id, fileName, fileType, sizeBytes, uploadedAt, owner, storageKey);
	}

	public boolean belongsToNotice() {
		return owner.type() == OwnerType.NOTICE;
	}

	public Long getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public FileType getFileType() {
		return fileType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public Instant getUploadedAt() {
		return uploadedAt;
	}

	public AttachmentOwner getOwner() {
		return owner;
	}

	public String getStorageKey() {
		return storageKey;
	}
}
