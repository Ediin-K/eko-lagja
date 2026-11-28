package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StubComplaintAccessTest {

	private final StubComplaintAccess complaintAccess = new StubComplaintAccess();

	@Test
	void nobodyMayViewOrAddComplaintPhotos() {
		for (Long userId : new Long[] { 1L, 3L, 21L, null }) {
			for (Long complaintId : new Long[] { 1L, 42L, null }) {
				assertThat(complaintAccess.canView(userId, complaintId)).isFalse();
				assertThat(complaintAccess.canAddPhoto(userId, complaintId)).isFalse();
			}
		}
	}
}
