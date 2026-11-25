package com.ekolagja.eko_lagja.complaints.domain;

import java.time.Instant;
import java.util.Objects;

public record OfficialResponse(
		String text,
		Instant respondedAt) {

	public OfficialResponse {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		Objects.requireNonNull(respondedAt, "respondedAt");
	}
}
