package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ekolagja.eko_lagja.notices.domain.OwnerType;

interface AttachmentJpaRepository extends JpaRepository<AttachmentJpaEntity, Long> {

	List<AttachmentJpaEntity> findByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType ownerType, Long ownerId);
}
