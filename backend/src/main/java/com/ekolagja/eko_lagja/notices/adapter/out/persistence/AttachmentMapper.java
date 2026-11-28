package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;

final class AttachmentMapper {

	private AttachmentMapper() {
	}

	static Attachment toDomain(AttachmentJpaEntity entity) {
		return Attachment.restore(entity.getId(), entity.getFileName(), entity.getFileType(), entity.getSizeBytes(),
				entity.getUploadedAt(), new AttachmentOwner(entity.getOwnerType(), entity.getOwnerId()),
				entity.getStorageKey());
	}

	static AttachmentJpaEntity toNewEntity(Attachment attachment) {
		return new AttachmentJpaEntity(attachment.getFileName(), attachment.getFileType(), attachment.getSizeBytes(),
				attachment.getUploadedAt(), attachment.getOwner().type(), attachment.getOwner().id(),
				attachment.getStorageKey());
	}
}
