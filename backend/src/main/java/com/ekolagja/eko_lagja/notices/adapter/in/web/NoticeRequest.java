package com.ekolagja.eko_lagja.notices.adapter.in.web;

import java.time.LocalDate;
import java.util.Set;

record NoticeRequest(
		String title,
		String content,
		String category,
		Set<Long> neighbourhoodIds,
		boolean urgent,
		LocalDate expiresOn) {

	Set<Long> neighbourhoodIdsOrEmpty() {
		return neighbourhoodIds == null ? Set.of() : neighbourhoodIds;
	}
}
