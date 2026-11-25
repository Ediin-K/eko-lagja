package com.ekolagja.eko_lagja.complaints.domain;

import java.time.Instant;
import java.util.Objects;

public record InternalNote(
		String text,
		Instant createdAt,
		Long authorId) {

	public InternalNote {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		Objects.requireNonNull(createdAt, "createdAt");
		Objects.requireNonNull(authorId, "authorId");
	}
}
