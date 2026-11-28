package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ekolagja.eko_lagja.notices.application.port.out.Notifications;
import com.ekolagja.eko_lagja.notices.domain.Notification;

@Component
class NotificationPersistenceAdapter implements Notifications {

	private final NotificationJpaRepository repository;

	NotificationPersistenceAdapter(NotificationJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void saveAll(List<Notification> notifications) {
		for (Notification notification : notifications) {
			if (notification.getId() == null) {
				repository.save(NotificationMapper.toNewEntity(notification));
			} else {
				NotificationJpaEntity entity = repository.findById(notification.getId())
						.orElseThrow(() -> new IllegalArgumentException(
								"notification " + notification.getId() + " does not exist"));
				entity.setReadAt(notification.getReadAt());
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Notification> findById(Long id) {
		return repository.findById(id).map(NotificationMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> findByRecipient(Long userId) {
		return repository.findByRecipientIdOrderByCreatedAtDescIdDesc(userId).stream()
				.map(NotificationMapper::toDomain)
				.toList();
	}
}
