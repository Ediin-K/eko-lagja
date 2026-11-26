package com.ekolagja.eko_lagja.notices.domain;

public class FileNotAllowedException extends RuntimeException {

	private final FileProblem reason;

	public FileNotAllowedException(FileProblem reason, String message) {
		super(message);
		this.reason = reason;
	}

	public FileProblem getReason() {
		return reason;
	}
}
