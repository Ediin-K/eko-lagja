package com.ekolagja.eko_lagja.notices.application.port.in;

public interface OpenAttachmentUseCase {

	FileContent open(Long attachmentId);

	FileContent openComplaintPhoto(Long complaintId, Long attachmentId);
}
