package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.util.Set;

import com.ekolagja.eko_lagja.notices.domain.Notice;

final class NoticeMapper {

	private NoticeMapper() {
	}

	static Notice toDomain(NoticeJpaEntity entity) {
		return Notice.restore(entity.getId(), entity.getTitle(), entity.getContent(), entity.getCategory(),
				Set.copyOf(entity.getNeighbourhoodIds()), entity.isUrgent(), entity.getPublishedBy(),
				entity.getPublishedAt(), entity.getExpiresOn(), entity.getWithdrawnAt());
	}

	static NoticeJpaEntity toNewEntity(Notice notice) {
		NoticeJpaEntity entity = new NoticeJpaEntity(notice.getPublishedBy(), notice.getPublishedAt());
		updateEntity(entity, notice);
		return entity;
	}

	static void updateEntity(NoticeJpaEntity entity, Notice notice) {
		entity.update(notice.getTitle(), notice.getContent(), notice.getCategory(), notice.getNeighbourhoodIds(),
				notice.isUrgent(), notice.getExpiresOn(), notice.getWithdrawnAt());
	}
}
