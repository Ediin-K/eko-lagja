package com.ekolagja.eko_lagja.complaints.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Complaint {

	private final Long id;
	private final String title;
	private final String description;
	private final String address;
	private final Double latitude;
	private final Double longitude;
	private final Instant submittedAt;
	private final Long residentId;
	private final Long categoryId;
	private final Long neighbourhoodId;
	private ComplaintStatus status;
	private final List<StatusChange> statusChanges;

	private Complaint(Long id, String title, String description, String address,
			Double latitude, Double longitude, Instant submittedAt,
			Long residentId, Long categoryId, Long neighbourhoodId,
			ComplaintStatus status, List<StatusChange> statusChanges) {
		this.id = id;
		this.title = requireText(title, "title");
		this.description = requireText(description, "description");
		this.address = requireText(address, "address");
		requireValidPin(latitude, longitude);
		this.latitude = latitude;
		this.longitude = longitude;
		this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
		this.residentId = Objects.requireNonNull(residentId, "residentId");
		this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
		this.neighbourhoodId = Objects.requireNonNull(neighbourhoodId, "neighbourhoodId");
		this.status = Objects.requireNonNull(status, "status");
		this.statusChanges = new ArrayList<>(statusChanges);
	}

	public static Complaint submit(String title, String description, String address,
			Double latitude, Double longitude, Long residentId, Long categoryId,
			Long neighbourhoodId, Instant submittedAt) {
		return new Complaint(null, title, description, address, latitude, longitude,
				submittedAt, residentId, categoryId, neighbourhoodId,
				ComplaintStatus.RECEIVED, List.of());
	}

	public StatusChange changeStatus(ComplaintStatus newStatus, String note, Instant changedAt) {
		Objects.requireNonNull(newStatus, "newStatus");
		if (!status.canChangeTo(newStatus)) {
			throw new StatusChangeNotAllowedException(status, newStatus);
		}
		StatusChange change = new StatusChange(status, newStatus, changedAt, note);
		boolean reopening = status.isClosed();
		if (reopening && change.note() == null) {
			throw new ReopenNoteRequiredException(status);
		}
		statusChanges.add(change);
		status = newStatus;
		return change;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getAddress() {
		return address;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public Long getResidentId() {
		return residentId;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public Long getNeighbourhoodId() {
		return neighbourhoodId;
	}

	public ComplaintStatus getStatus() {
		return status;
	}

	public List<StatusChange> getStatusChanges() {
		return List.copyOf(statusChanges);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
	}

	private static void requireValidPin(Double latitude, Double longitude) {
		if (latitude == null && longitude == null) {
			return;
		}
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("latitude and longitude must be given together");
		}
		if (!(latitude >= -90 && latitude <= 90) || !(longitude >= -180 && longitude <= 180)) {
			throw new IllegalArgumentException("latitude or longitude out of range");
		}
	}
}
