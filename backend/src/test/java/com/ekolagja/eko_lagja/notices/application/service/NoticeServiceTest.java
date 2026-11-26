package com.ekolagja.eko_lagja.notices.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ekolagja.eko_lagja.notices.application.port.in.CreateNotificationUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.EditNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeView;
import com.ekolagja.eko_lagja.notices.application.port.in.PublishNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.NeighbourhoodDirectory;
import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.application.port.out.ResidentDirectory;
import com.ekolagja.eko_lagja.notices.domain.InvalidNoticeException;
import com.ekolagja.eko_lagja.notices.domain.Notice;
import com.ekolagja.eko_lagja.notices.domain.NoticeNotActiveException;
import com.ekolagja.eko_lagja.notices.domain.NoticeStatus;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;

class NoticeServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 26);
	private static final LocalDate NEXT_WEEK = TODAY.plusDays(7);
	private static final Long OFFICIAL_ID = 3L;
	private static final Long CENTER = 5L;
	private static final Long SUNNY_HILL = 9L;

	private final InMemoryNotices notices = new InMemoryNotices();
	private final FakeCurrentUser currentUser = new FakeCurrentUser();
	private final FakeResidents residents = new FakeResidents();
	private final RecordingNotifications notifications = new RecordingNotifications();
	private final NoticeService service = new NoticeService(notices, currentUser,
			ids -> Set.of(CENTER, SUNNY_HILL).containsAll(ids), residents, notifications,
			Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void officialPublishesAnActiveNotice() {
		currentUser.logInAsOfficial();

		Long id = service.publish(command(Set.of(CENTER), false));

		NoticeView view = service.get(id);
		assertThat(view.title()).isEqualTo("Water cut");
		assertThat(view.neighbourhoodIds()).containsExactly(CENTER);
		assertThat(view.publishedAt()).isEqualTo(NOW);
		assertThat(notices.byId(id).getPublishedBy()).isEqualTo(OFFICIAL_ID);
	}

	@Test
	void residentsAndVisitorsCannotPublish() {
		currentUser.logInAsResident();
		assertThatThrownBy(() -> service.publish(command(Set.of(), false)))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logOut();
		assertThatThrownBy(() -> service.publish(command(Set.of(), false)))
				.isInstanceOf(NotAllowedException.class);

		assertThat(notices.count()).isZero();
	}

	@Test
	void unknownNeighbourhoodIsRefused() {
		currentUser.logInAsOfficial();

		assertThatThrownBy(() -> service.publish(command(Set.of(77L), false)))
				.isInstanceOf(InvalidNoticeException.class);
		assertThat(notices.count()).isZero();
	}

	@Test
	void urgentNoticeNotifiesTheResidentsOfItsNeighbourhoods() {
		currentUser.logInAsOfficial();
		residents.live(CENTER, 21L, 22L);
		residents.live(SUNNY_HILL, 31L);

		Long id = service.publish(command(Set.of(CENTER), true));

		assertThat(notifications.calls).containsExactly(
				new NotificationCall(NotificationType.NEW_NOTICE, Set.of(21L, 22L), id));
	}

	@Test
	void urgentWholeMunicipalityNoticeNotifiesEveryResident() {
		currentUser.logInAsOfficial();
		residents.live(CENTER, 21L);
		residents.live(SUNNY_HILL, 31L);

		Long id = service.publish(command(Set.of(), true));

		assertThat(notifications.calls).containsExactly(
				new NotificationCall(NotificationType.NEW_NOTICE, Set.of(21L, 31L), id));
	}

	@Test
	void normalNoticeDoesNotRingTheBell() {
		currentUser.logInAsOfficial();
		residents.live(CENTER, 21L);

		service.publish(command(Set.of(CENTER), false));

		assertThat(notifications.calls).isEmpty();
	}

	@Test
	void listShowsVisibleNoticesUrgentFirstThenNewest() {
		Long whole = notices.seed(Set.of(), false, NOW.minusSeconds(7200), NEXT_WEEK, null);
		Long centerOld = notices.seed(Set.of(CENTER), false, NOW.minusSeconds(3600), NEXT_WEEK, null);
		Long centerUrgent = notices.seed(Set.of(CENTER), true, NOW.minusSeconds(86400), NEXT_WEEK, null);
		notices.seed(Set.of(SUNNY_HILL), false, NOW, NEXT_WEEK, null);
		notices.seed(Set.of(CENTER), false, NOW, TODAY.minusDays(1), null);
		notices.seed(Set.of(CENTER), false, NOW, NEXT_WEEK, NOW);

		assertThat(service.list(CENTER)).extracting(NoticeView::id)
				.containsExactly(centerUrgent, centerOld, whole);
		assertThat(service.list(null)).extracting(NoticeView::id)
				.containsExactly(whole);
	}

	@Test
	void expiredWithdrawnOrMissingNoticesAreNotFound() {
		Long expired = notices.seed(Set.of(), false, NOW, TODAY.minusDays(1), null);
		Long withdrawn = notices.seed(Set.of(), false, NOW, NEXT_WEEK, NOW);

		assertThatThrownBy(() -> service.get(expired)).isInstanceOf(NoticeNotFoundException.class);
		assertThatThrownBy(() -> service.get(withdrawn)).isInstanceOf(NoticeNotFoundException.class);
		assertThatThrownBy(() -> service.get(404L)).isInstanceOf(NoticeNotFoundException.class);
	}

	@Test
	void officialEditsAnActiveNotice() {
		Long id = notices.seed(Set.of(CENTER), false, NOW, NEXT_WEEK, null);
		currentUser.logInAsOfficial();

		service.edit(new EditNoticeCommand(id, "Water cut moved", "Now on Tuesday.", "WATER",
				Set.of(SUNNY_HILL), false, TODAY));

		NoticeView view = service.get(id);
		assertThat(view.title()).isEqualTo("Water cut moved");
		assertThat(view.neighbourhoodIds()).containsExactly(SUNNY_HILL);
		assertThat(view.expiresOn()).isEqualTo(TODAY);
	}

	@Test
	void onlyOfficialsEditAndOnlyActiveNotices() {
		Long active = notices.seed(Set.of(), false, NOW, NEXT_WEEK, null);
		Long expired = notices.seed(Set.of(), false, NOW, TODAY.minusDays(1), null);

		currentUser.logInAsResident();
		assertThatThrownBy(() -> service.edit(editOf(active)))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logInAsOfficial();
		assertThatThrownBy(() -> service.edit(editOf(expired)))
				.isInstanceOf(NoticeNotActiveException.class);
		assertThat(notices.byId(active).getTitle()).isEqualTo("Water cut");
	}

	@Test
	void officialWithdrawsANoticeAndItLeavesTheList() {
		Long id = notices.seed(Set.of(), false, NOW, NEXT_WEEK, null);
		currentUser.logInAsOfficial();

		service.withdraw(id);

		assertThat(notices.byId(id).statusOn(TODAY)).isEqualTo(NoticeStatus.WITHDRAWN);
		assertThat(service.list(null)).isEmpty();
	}

	@Test
	void residentsCannotWithdraw() {
		Long id = notices.seed(Set.of(), false, NOW, NEXT_WEEK, null);
		currentUser.logInAsResident();

		assertThatThrownBy(() -> service.withdraw(id)).isInstanceOf(NotAllowedException.class);
		assertThat(notices.byId(id).getWithdrawnAt()).isNull();
	}

	private static PublishNoticeCommand command(Set<Long> neighbourhoodIds, boolean urgent) {
		return new PublishNoticeCommand("Water cut", "No water from 08:00 to 14:00.", "WATER",
				neighbourhoodIds, urgent, NEXT_WEEK);
	}

	private static EditNoticeCommand editOf(Long id) {
		return new EditNoticeCommand(id, "New title", "New content.", "WATER", Set.of(), false, NEXT_WEEK);
	}

	private record NotificationCall(NotificationType type, Set<Long> recipientIds, Long itemId) {
	}

	private static class RecordingNotifications implements CreateNotificationUseCase {

		private final List<NotificationCall> calls = new ArrayList<>();

		@Override
		public void create(NotificationType type, Set<Long> recipientIds, Long itemId) {
			calls.add(new NotificationCall(type, Set.copyOf(recipientIds), itemId));
		}
	}

	private static class FakeCurrentUser implements CurrentUser {

		private Long id;
		private boolean official;

		void logInAsOfficial() {
			id = OFFICIAL_ID;
			official = true;
		}

		void logInAsResident() {
			id = 21L;
			official = false;
		}

		void logOut() {
			id = null;
			official = false;
		}

		@Override
		public Optional<Long> id() {
			return Optional.ofNullable(id);
		}

		@Override
		public boolean isOfficial() {
			return official;
		}
	}

	private static class FakeResidents implements ResidentDirectory {

		private final Map<Long, Set<Long>> byNeighbourhood = new HashMap<>();

		void live(Long neighbourhoodId, Long... residentIds) {
			byNeighbourhood.computeIfAbsent(neighbourhoodId, k -> new HashSet<>()).addAll(List.of(residentIds));
		}

		@Override
		public Set<Long> residentIdsIn(Set<Long> neighbourhoodIds) {
			Set<Long> result = new HashSet<>();
			byNeighbourhood.forEach((neighbourhood, ids) -> {
				if (neighbourhoodIds.isEmpty() || neighbourhoodIds.contains(neighbourhood)) {
					result.addAll(ids);
				}
			});
			return result;
		}
	}

	private static class InMemoryNotices implements Notices {

		private final Map<Long, Notice> byId = new HashMap<>();
		private long nextId = 1;

		Long seed(Set<Long> neighbourhoodIds, boolean urgent, Instant publishedAt,
				LocalDate expiresOn, Instant withdrawnAt) {
			Long id = nextId++;
			byId.put(id, Notice.restore(id, "Water cut", "No water.", "WATER", neighbourhoodIds,
					urgent, OFFICIAL_ID, publishedAt, expiresOn, withdrawnAt));
			return id;
		}

		Notice byId(Long id) {
			return byId.get(id);
		}

		int count() {
			return byId.size();
		}

		@Override
		public Notice save(Notice notice) {
			Notice stored = notice.getId() != null ? notice
					: Notice.restore(nextId++, notice.getTitle(), notice.getContent(),
							notice.getCategory(), notice.getNeighbourhoodIds(), notice.isUrgent(),
							notice.getPublishedBy(), notice.getPublishedAt(), notice.getExpiresOn(),
							notice.getWithdrawnAt());
			byId.put(stored.getId(), stored);
			return stored;
		}

		@Override
		public Optional<Notice> findById(Long id) {
			return Optional.ofNullable(byId.get(id));
		}

		@Override
		public List<Notice> findActiveFor(Long neighbourhoodId, LocalDate today) {
			return List.copyOf(byId.values());
		}
	}
}
