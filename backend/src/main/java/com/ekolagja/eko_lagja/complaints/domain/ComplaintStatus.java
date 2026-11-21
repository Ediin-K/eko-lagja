package com.ekolagja.eko_lagja.complaints.domain;

public enum ComplaintStatus {
	RECEIVED,
	IN_PROGRESS,
	RESOLVED,
	REJECTED;

	public boolean canChangeTo(ComplaintStatus next) {
		return switch (this) {
			case RECEIVED -> next == IN_PROGRESS || next == REJECTED;
			case IN_PROGRESS -> next == RESOLVED || next == REJECTED;
			case RESOLVED, REJECTED -> next == IN_PROGRESS;
		};
	}

	public boolean isClosed() {
		return this == RESOLVED || this == REJECTED;
	}
}
