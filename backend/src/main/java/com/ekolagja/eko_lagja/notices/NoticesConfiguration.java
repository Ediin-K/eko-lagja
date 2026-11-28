package com.ekolagja.eko_lagja.notices;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.application.port.out.ComplaintAccess;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.FileStore;
import com.ekolagja.eko_lagja.notices.application.port.out.NeighbourhoodDirectory;
import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.application.port.out.Notifications;
import com.ekolagja.eko_lagja.notices.application.port.out.ResidentDirectory;
import com.ekolagja.eko_lagja.notices.application.service.AttachmentService;
import com.ekolagja.eko_lagja.notices.application.service.NoticeService;
import com.ekolagja.eko_lagja.notices.application.service.NotificationService;

@Configuration
class NoticesConfiguration {

	private final Clock clock = Clock.systemUTC();

	@Bean
	NotificationService notificationService(Notifications notifications, CurrentUser currentUser) {
		return new NotificationService(notifications, currentUser, clock);
	}

	@Bean
	NoticeService noticeService(Notices notices, Attachments attachments, CurrentUser currentUser,
			NeighbourhoodDirectory neighbourhoods, ResidentDirectory residents,
			NotificationService notificationService) {
		return new NoticeService(notices, attachments, currentUser, neighbourhoods, residents,
				notificationService, clock);
	}

	@Bean
	AttachmentService attachmentService(Attachments attachments, FileStore fileStore, Notices notices,
			ComplaintAccess complaintAccess, CurrentUser currentUser) {
		return new AttachmentService(attachments, fileStore, notices, complaintAccess, currentUser, clock);
	}
}
