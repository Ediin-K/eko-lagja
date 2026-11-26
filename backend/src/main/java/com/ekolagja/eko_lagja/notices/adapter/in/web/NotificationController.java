package com.ekolagja.eko_lagja.notices.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ekolagja.eko_lagja.notices.application.port.in.ListMyNotificationsUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.MarkNotificationReadUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.MyNotifications;

@RestController
@RequestMapping("/api/notifications")
class NotificationController {

	private final ListMyNotificationsUseCase listMyNotifications;
	private final MarkNotificationReadUseCase markNotificationRead;

	NotificationController(ListMyNotificationsUseCase listMyNotifications,
			MarkNotificationReadUseCase markNotificationRead) {
		this.listMyNotifications = listMyNotifications;
		this.markNotificationRead = markNotificationRead;
	}

	@GetMapping
	MyNotifications mine() {
		return listMyNotifications.listMine();
	}

	@PostMapping("/{id}/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void markRead(@PathVariable Long id) {
		markNotificationRead.markRead(id);
	}
}
