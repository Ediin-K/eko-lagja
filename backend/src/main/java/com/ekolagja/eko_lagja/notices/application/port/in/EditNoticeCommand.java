package com.ekolagja.eko_lagja.notices.application.port.in;

import java.time.LocalDate;
import java.util.Set;

public record EditNoticeCommand(
		Long noticeId,
		String title,
		String content,
		String category,
		Set<Long> neighbourhoodIds,
		boolean urgent,
		LocalDate expiresOn) {
}
