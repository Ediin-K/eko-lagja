package com.ekolagja.eko_lagja.notices.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import com.ekolagja.eko_lagja.notices.domain.Notice;

public record NoticeView(
		Long id,
		String title,
		String content,
		String category,
		Set<Long> neighbourhoodIds,
		boolean urgent,
		Instant publishedAt,
		LocalDate expiresOn) {

	public static NoticeView of(Notice notice) {
		return new NoticeView(notice.getId(), notice.getTitle(), notice.getContent(),
				notice.getCategory(), notice.getNeighbourhoodIds(), notice.isUrgent(),
				notice.getPublishedAt(), notice.getExpiresOn());
	}
}
