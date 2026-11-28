package com.ekolagja.eko_lagja.notices.adapter.in.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ekolagja.eko_lagja.notices.application.port.in.AttachmentNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NotificationNotFoundException;
import com.ekolagja.eko_lagja.notices.domain.FileNotAllowedException;
import com.ekolagja.eko_lagja.notices.domain.InvalidNoticeException;
import com.ekolagja.eko_lagja.notices.domain.NoticeNotActiveException;

@RestControllerAdvice(assignableTypes = { NoticeController.class, NotificationController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
class NoticesExceptionHandler {

	@ExceptionHandler(NotAllowedException.class)
	ResponseEntity<ErrorResponse> notAllowed() {
		return error(HttpStatus.FORBIDDEN, "NOT_ALLOWED");
	}

	@ExceptionHandler({ NoticeNotFoundException.class, NotificationNotFoundException.class,
			AttachmentNotFoundException.class })
	ResponseEntity<ErrorResponse> notFound() {
		return error(HttpStatus.NOT_FOUND, "NOT_FOUND");
	}

	@ExceptionHandler(InvalidNoticeException.class)
	ResponseEntity<ErrorResponse> invalidNotice(InvalidNoticeException exception) {
		return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_NOTICE", exception.getField(), null));
	}

	@ExceptionHandler(FileNotAllowedException.class)
	ResponseEntity<ErrorResponse> fileNotAllowed(FileNotAllowedException exception) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse("FILE_NOT_ALLOWED", null, exception.getReason().name()));
	}

	@ExceptionHandler(NoticeNotActiveException.class)
	ResponseEntity<ErrorResponse> noticeNotActive() {
		return error(HttpStatus.CONFLICT, "NOTICE_NOT_ACTIVE");
	}

	private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code) {
		return ResponseEntity.status(status).body(ErrorResponse.of(code));
	}
}
