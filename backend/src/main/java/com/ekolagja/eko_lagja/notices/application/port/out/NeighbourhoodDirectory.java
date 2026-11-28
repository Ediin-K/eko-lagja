package com.ekolagja.eko_lagja.notices.application.port.out;

import java.util.Set;

public interface NeighbourhoodDirectory {

	boolean allExist(Set<Long> neighbourhoodIds);
}
