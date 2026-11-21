package com.ekolagja.eko_lagja.complaints.domain;

public class StatusChangeNotAllowedException extends RuntimeException {

	private final ComplaintStatus fromStatus;
	private final ComplaintStatus toStatus;

	public StatusChangeNotAllowedException(ComplaintStatus fromStatus, ComplaintStatus toStatus) {
		super("Status change not allowed: " + fromStatus + " -> " + toStatus);
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
	}

	public ComplaintStatus getFromStatus() {
		return fromStatus;
	}

	public ComplaintStatus getToStatus() {
		return toStatus;
	}
}
