package com.ekolagja.eko_lagja.notices.application.port.out;

import java.util.Optional;

public interface CurrentUser {

	Optional<Long> id();

	boolean isOfficial();
}
