package com.ekolagja.eko_lagja.notices.application.port.in;

public class AttachmentNotFoundException extends RuntimeException {

	private final Long attachmentId;

	public AttachmentNotFoundException(Long attachmentId) {
		super("Attachment not found: " + attachmentId);
		this.attachmentId = attachmentId;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}
}
