package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import org.springframework.stereotype.Component;

import com.ekolagja.eko_lagja.notices.application.port.out.ComplaintAccess;

@Component("noticesStubComplaintAccess")
class StubComplaintAccess implements ComplaintAccess {

	@Override
	public boolean canView(Long userId, Long complaintId) {
		return false;
	}

	@Override
	public boolean canAddPhoto(Long userId, Long complaintId) {
		return false;
	}
}
