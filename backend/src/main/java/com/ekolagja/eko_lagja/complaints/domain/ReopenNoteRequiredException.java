package com.ekolagja.eko_lagja.complaints.domain;

public class ReopenNoteRequiredException extends RuntimeException {

	private final ComplaintStatus fromStatus;

	public ReopenNoteRequiredException(ComplaintStatus fromStatus) {
		super("Reopening a " + fromStatus + " complaint requires a note");
		this.fromStatus = fromStatus;
	}

	public ComplaintStatus getFromStatus() {
		return fromStatus;
	}
}
