package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;

@Component("noticesStubAnonymousCurrentUser")
@Profile("!dev | prod")
class StubAnonymousCurrentUser implements CurrentUser {

	@Override
	public Optional<Long> id() {
		return Optional.empty();
	}

	@Override
	public boolean isOfficial() {
		return false;
	}
}
