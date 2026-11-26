package com.ekolagja.eko_lagja.notices.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.ekolagja.eko_lagja.notices.application.port.in.CreateNotificationUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.EditNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.in.EditNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.GetNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.ListNoticesUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeDetails;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeFileView;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeView;
import com.ekolagja.eko_lagja.notices.application.port.in.PublishNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.in.PublishNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.WithdrawNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.NeighbourhoodDirectory;
import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.application.port.out.ResidentDirectory;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;
import com.ekolagja.eko_lagja.notices.domain.InvalidNoticeException;
import com.ekolagja.eko_lagja.notices.domain.Notice;
import com.ekolagja.eko_lagja.notices.domain.NoticeStatus;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

public class NoticeService implements PublishNoticeUseCase, EditNoticeUseCase,
		WithdrawNoticeUseCase, ListNoticesUseCase, GetNoticeUseCase {

	private static final Comparator<Notice> URGENT_FIRST_THEN_NEWEST =
			Comparator.comparing(Notice::isUrgent).reversed()
					.thenComparing(Notice::getPublishedAt, Comparator.reverseOrder());

	private final Notices notices;
	private final Attachments attachments;
	private final CurrentUser currentUser;
	private final NeighbourhoodDirectory neighbourhoods;
	private final ResidentDirectory residents;
	private final CreateNotificationUseCase notifications;
	private final Clock clock;

	public NoticeService(Notices notices, Attachments attachments, CurrentUser currentUser,
			NeighbourhoodDirectory neighbourhoods, ResidentDirectory residents,
			CreateNotificationUseCase notifications, Clock clock) {
		this.notices = notices;
		this.attachments = attachments;
		this.currentUser = currentUser;
		this.neighbourhoods = neighbourhoods;
		this.residents = residents;
		this.notifications = notifications;
		this.clock = clock;
	}

	@Override
	public Long publish(PublishNoticeCommand command) {
		Long officialId = requireOfficial();
		requireKnownNeighbourhoods(command.neighbourhoodIds());
		Notice notice = Notice.publish(command.title(), command.content(), command.category(),
				command.neighbourhoodIds(), command.urgent(), command.expiresOn(),
				officialId, Instant.now(clock));
		Notice saved = notices.save(notice);
		if (saved.isUrgent()) {
			Set<Long> recipients = residents.residentIdsIn(saved.getNeighbourhoodIds());
			if (!recipients.isEmpty()) {
				notifications.create(NotificationType.NEW_NOTICE, recipients, saved.getId());
			}
		}
		return saved.getId();
	}

	@Override
	public void edit(EditNoticeCommand command) {
		requireOfficial();
		Notice notice = load(command.noticeId());
		requireKnownNeighbourhoods(command.neighbourhoodIds());
		notice.edit(command.title(), command.content(), command.category(),
				command.neighbourhoodIds(), command.urgent(), command.expiresOn(), today());
		notices.save(notice);
	}

	@Override
	public void withdraw(Long noticeId) {
		requireOfficial();
		Notice notice = load(noticeId);
		notice.withdraw(Instant.now(clock));
		notices.save(notice);
	}

	@Override
	public List<NoticeView> list(Long neighbourhoodId) {
		LocalDate today = today();
		return notices.findActiveFor(neighbourhoodId, today).stream()
				.filter(notice -> notice.isVisibleIn(neighbourhoodId, today))
				.sorted(URGENT_FIRST_THEN_NEWEST)
				.map(NoticeView::of)
				.toList();
	}

	@Override
	public NoticeDetails get(Long noticeId) {
		Notice notice = load(noticeId);
		if (notice.statusOn(today()) != NoticeStatus.ACTIVE) {
			throw new NoticeNotFoundException(noticeId);
		}
		List<NoticeFileView> files = attachments.findByOwner(AttachmentOwner.ofNotice(noticeId)).stream()
				.map(NoticeFileView::of)
				.toList();
		return new NoticeDetails(NoticeView.of(notice), files);
	}

	private Long requireOfficial() {
		if (!currentUser.isOfficial()) {
			throw new NotAllowedException("only municipal officials can manage notices");
		}
		return currentUser.id().orElseThrow(() -> new NotAllowedException("no logged-in user"));
	}

	private void requireKnownNeighbourhoods(Set<Long> neighbourhoodIds) {
		if (neighbourhoodIds != null && !neighbourhoodIds.isEmpty()
				&& !neighbourhoods.allExist(neighbourhoodIds)) {
			throw new InvalidNoticeException("neighbourhoodIds", "unknown neighbourhood");
		}
	}

	private Notice load(Long noticeId) {
		return notices.findById(noticeId).orElseThrow(() -> new NoticeNotFoundException(noticeId));
	}

	private LocalDate today() {
		return LocalDate.ofInstant(Instant.now(clock), Notice.MUNICIPALITY_ZONE);
	}
}
