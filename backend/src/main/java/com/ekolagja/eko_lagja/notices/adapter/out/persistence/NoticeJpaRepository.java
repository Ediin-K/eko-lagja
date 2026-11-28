package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NoticeJpaRepository extends JpaRepository<NoticeJpaEntity, Long> {

	@Query("""
			select distinct n from NoticeJpaEntity n left join n.neighbourhoodIds neighbourhood
			where n.withdrawnAt is null and n.expiresOn >= :today
			and (n.neighbourhoodIds is empty or neighbourhood = :neighbourhoodId)
			""")
	List<NoticeJpaEntity> findActiveFor(@Param("neighbourhoodId") Long neighbourhoodId,
			@Param("today") LocalDate today);
}
