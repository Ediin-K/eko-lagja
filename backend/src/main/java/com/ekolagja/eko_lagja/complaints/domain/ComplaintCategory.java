package com.ekolagja.eko_lagja.complaints.domain;

import java.util.Objects;

public record ComplaintCategory(
		Long id,
		String name,
		String description,
		Long departmentId) {

	public ComplaintCategory {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("description must not be blank");
		}
		Objects.requireNonNull(departmentId, "departmentId");
	}
}
