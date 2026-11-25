package com.ekolagja.eko_lagja.complaints.domain;

import java.time.Instant;
import java.util.Objects;

public record StatusChange(
		ComplaintStatus fromStatus,
		ComplaintStatus toStatus,
		Instant changedAt,
		String note) {

	public StatusChange {
		Objects.requireNonNull(fromStatus, "fromStatus");
		Objects.requireNonNull(toStatus, "toStatus");
		Objects.requireNonNull(changedAt, "changedAt");
		note = (note == null || note.isBlank()) ? null : note.strip();
	}
}
