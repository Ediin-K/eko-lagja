package com.ekolagja.eko_lagja.notices.application.port.out;

public interface ComplaintAccess {

	boolean canView(Long userId, Long complaintId);

	boolean canAddPhoto(Long userId, Long complaintId);
}
