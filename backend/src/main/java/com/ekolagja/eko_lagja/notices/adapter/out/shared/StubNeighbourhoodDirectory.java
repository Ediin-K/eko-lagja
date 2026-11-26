package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.ekolagja.eko_lagja.notices.application.port.out.NeighbourhoodDirectory;

@Component("noticesStubNeighbourhoodDirectory")
class StubNeighbourhoodDirectory implements NeighbourhoodDirectory {

	@Override
	public boolean allExist(Set<Long> neighbourhoodIds) {
		return neighbourhoodIds.stream().allMatch(id -> id != null && id > 0);
	}
}
