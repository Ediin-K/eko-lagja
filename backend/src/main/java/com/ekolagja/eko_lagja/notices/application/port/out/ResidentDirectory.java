package com.ekolagja.eko_lagja.notices.application.port.out;

import java.util.Set;

public interface ResidentDirectory {

	Set<Long> residentIdsIn(Set<Long> neighbourhoodIds);
}
