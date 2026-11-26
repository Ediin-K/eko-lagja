package com.ekolagja.eko_lagja.notices.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ErrorResponse(String error, String field, String reason) {

	static ErrorResponse of(String error) {
		return new ErrorResponse(error, null, null);
	}
}
