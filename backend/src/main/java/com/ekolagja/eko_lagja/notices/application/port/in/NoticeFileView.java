package com.ekolagja.eko_lagja.notices.application.port.in;

import com.ekolagja.eko_lagja.notices.domain.Attachment;

public record NoticeFileView(Long id, String fileName, String contentType, long sizeBytes) {

	public static NoticeFileView of(Attachment attachment) {
		return new NoticeFileView(attachment.getId(), attachment.getFileName(),
				attachment.getFileType().getContentType(), attachment.getSizeBytes());
	}
}
