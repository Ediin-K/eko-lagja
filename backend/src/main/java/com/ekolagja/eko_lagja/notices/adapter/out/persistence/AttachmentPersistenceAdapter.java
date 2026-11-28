package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;

@Component
class AttachmentPersistenceAdapter implements Attachments {

	private final AttachmentJpaRepository repository;

	AttachmentPersistenceAdapter(AttachmentJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Attachment save(Attachment attachment) {
		if (attachment.getId() != null) {
			throw new IllegalArgumentException("attachment " + attachment.getId() + " is already saved and never changes");
		}
		return AttachmentMapper.toDomain(repository.save(AttachmentMapper.toNewEntity(attachment)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Attachment> findById(Long id) {
		return repository.findById(id).map(AttachmentMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Attachment> findByOwner(AttachmentOwner owner) {
		return repository.findByOwnerTypeAndOwnerIdOrderByIdAsc(owner.type(), owner.id()).stream()
				.map(AttachmentMapper::toDomain)
				.toList();
	}
}
