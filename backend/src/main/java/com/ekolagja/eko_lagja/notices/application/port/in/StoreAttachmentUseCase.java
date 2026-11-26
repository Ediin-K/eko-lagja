package com.ekolagja.eko_lagja.notices.application.port.in;

import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;

public interface StoreAttachmentUseCase {

	Long store(AttachmentOwner owner, FileUpload upload);
}
