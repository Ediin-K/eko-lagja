package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.domain.Notice;

@Component
class NoticePersistenceAdapter implements Notices {

	private final NoticeJpaRepository repository;

	NoticePersistenceAdapter(NoticeJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Notice save(Notice notice) {
		NoticeJpaEntity entity;
		if (notice.getId() == null) {
			entity = NoticeMapper.toNewEntity(notice);
		} else {
			entity = repository.findById(notice.getId())
					.orElseThrow(() -> new IllegalArgumentException("notice " + notice.getId() + " does not exist"));
			NoticeMapper.updateEntity(entity, notice);
		}
		return NoticeMapper.toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Notice> findById(Long id) {
		return repository.findById(id).map(NoticeMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notice> findActiveFor(Long neighbourhoodId, LocalDate today) {
		return repository.findActiveFor(neighbourhoodId, today).stream()
				.map(NoticeMapper::toDomain)
				.toList();
	}
}
