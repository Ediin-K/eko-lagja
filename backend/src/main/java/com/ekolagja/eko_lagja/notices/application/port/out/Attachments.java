package com.ekolagja.eko_lagja.notices.application.port.out;

import java.util.List;
import java.util.Optional;

import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;

public interface Attachments {

	Attachment save(Attachment attachment);

	Optional<Attachment> findById(Long id);

	List<Attachment> findByOwner(AttachmentOwner owner);
}
