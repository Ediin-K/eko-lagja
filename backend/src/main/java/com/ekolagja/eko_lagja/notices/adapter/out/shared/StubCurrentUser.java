package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;

@Component("noticesStubCurrentUser")
@Profile("dev & !prod")
class StubCurrentUser implements CurrentUser {

	static final String USER_ID_HEADER = "X-Stub-User-Id";
	static final String ROLE_HEADER = "X-Stub-Role";

	@Override
	public Optional<Long> id() {
		return header(USER_ID_HEADER).flatMap(StubCurrentUser::parseId);
	}

	@Override
	public boolean isOfficial() {
		return id().isPresent() && header(ROLE_HEADER).map("OFFICIAL"::equalsIgnoreCase).orElse(false);
	}

	private static Optional<Long> parseId(String value) {
		try {
			long id = Long.parseLong(value);
			return id > 0 ? Optional.of(id) : Optional.empty();
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	private static Optional<String> header(String name) {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servlet) {
			return Optional.ofNullable(servlet.getRequest().getHeader(name))
					.map(String::strip)
					.filter(value -> !value.isEmpty());
		}
		return Optional.empty();
	}
}
