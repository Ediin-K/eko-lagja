package com.ekolagja.eko_lagja.notices.domain;

import java.util.Locale;

public enum FileType {
	JPEG("image/jpeg"),
	PNG("image/png"),
	PDF("application/pdf");

	private final String contentType;

	FileType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public static FileType fromContentType(String contentType) {
		if (contentType == null) {
			throw new FileNotAllowedException("content type is missing");
		}
		String mediaType = contentType.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
		for (FileType type : values()) {
			if (type.contentType.equals(mediaType)) {
				return type;
			}
		}
		throw new FileNotAllowedException("file type not allowed: " + contentType);
	}
}
