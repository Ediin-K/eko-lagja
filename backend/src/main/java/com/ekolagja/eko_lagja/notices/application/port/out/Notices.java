package com.ekolagja.eko_lagja.notices.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ekolagja.eko_lagja.notices.domain.Notice;

public interface Notices {

	Notice save(Notice notice);

	Optional<Notice> findById(Long id);

	List<Notice> findActiveFor(Long neighbourhoodId, LocalDate today);
}
