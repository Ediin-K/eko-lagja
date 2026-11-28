package com.ekolagja.eko_lagja.notices.application.port.out;

import java.util.List;
import java.util.Optional;

import com.ekolagja.eko_lagja.notices.domain.Notification;

public interface Notifications {

	void saveAll(List<Notification> notifications);

	Optional<Notification> findById(Long id);

	List<Notification> findByRecipient(Long userId);
}
