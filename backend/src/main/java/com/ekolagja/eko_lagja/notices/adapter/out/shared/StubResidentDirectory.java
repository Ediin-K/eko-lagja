package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.ekolagja.eko_lagja.notices.application.port.out.ResidentDirectory;

@Component("noticesStubResidentDirectory")
class StubResidentDirectory implements ResidentDirectory {

	@Override
	public Set<Long> residentIdsIn(Set<Long> neighbourhoodIds) {
		return Set.of();
	}
}
