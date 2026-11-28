package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

	List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId);
}
