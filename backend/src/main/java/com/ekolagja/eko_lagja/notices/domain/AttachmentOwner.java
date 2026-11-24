package com.ekolagja.eko_lagja.notices.domain;

import java.util.Objects;

public record AttachmentOwner(OwnerType type, Long id) {

	public AttachmentOwner {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(id, "id");
	}

	public static AttachmentOwner ofNotice(Long noticeId) {
		return new AttachmentOwner(OwnerType.NOTICE, noticeId);
	}

	public static AttachmentOwner ofComplaint(Long complaintId) {
		return new AttachmentOwner(OwnerType.COMPLAINT, complaintId);
	}
}
