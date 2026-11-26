package com.ekolagja.eko_lagja.notices.domain;

import static com.ekolagja.eko_lagja.notices.domain.NoticeStatus.ACTIVE;
import static com.ekolagja.eko_lagja.notices.domain.NoticeStatus.EXPIRED;
import static com.ekolagja.eko_lagja.notices.domain.NoticeStatus.WITHDRAWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class NoticeTest {

	private static final String TITLE = "Water cut on Monday";
	private static final String CONTENT = "There will be no water from 08:00 to 14:00.";
	private static final String CATEGORY = "WATER";
	private static final Long OFFICIAL_ID = 3L;
	private static final Long NEIGHBOURHOOD_ID = 5L;
	private static final Long OTHER_NEIGHBOURHOOD_ID = 9L;
	private static final Instant NOW = Instant.parse("2026-11-23T09:00:00Z");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 23);
	private static final LocalDate EXPIRES_ON = LocalDate.of(2026, 11, 30);

	@Test
	void publishedNoticeIsActiveAndKeepsItsData() {
		Notice notice = publishFor(Set.of(NEIGHBOURHOOD_ID));

		assertThat(notice.getId()).isNull();
		assertThat(notice.getTitle()).isEqualTo(TITLE);
		assertThat(notice.getContent()).isEqualTo(CONTENT);
		assertThat(notice.getCategory()).isEqualTo(CATEGORY);
		assertThat(notice.getNeighbourhoodIds()).containsExactly(NEIGHBOURHOOD_ID);
		assertThat(notice.isUrgent()).isTrue();
		assertThat(notice.getPublishedBy()).isEqualTo(OFFICIAL_ID);
		assertThat(notice.getPublishedAt()).isEqualTo(NOW);
		assertThat(notice.getExpiresOn()).isEqualTo(EXPIRES_ON);
		assertThat(notice.getWithdrawnAt()).isNull();
		assertThat(notice.statusOn(TODAY)).isEqualTo(ACTIVE);
	}

	@Test
	void titleAndContentMustNotBeBlank() {
		assertThatThrownBy(() -> Notice.publish(" ", CONTENT, CATEGORY, Set.of(), false, EXPIRES_ON, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
		assertThatThrownBy(() -> Notice.publish(null, CONTENT, CATEGORY, Set.of(), false, EXPIRES_ON, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
		assertThatThrownBy(() -> Notice.publish(TITLE, "", CATEGORY, Set.of(), false, EXPIRES_ON, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
	}

	@Test
	void publisherMustBeSet() {
		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, Set.of(), false, EXPIRES_ON, null, NOW))
				.isInstanceOf(InvalidNoticeException.class);
	}

	@Test
	void noticeCanBeValidForTodayOnly() {
		Notice notice = Notice.publish(TITLE, CONTENT, CATEGORY, Set.of(), false, TODAY, OFFICIAL_ID, NOW);

		assertThat(notice.statusOn(TODAY)).isEqualTo(ACTIVE);
	}

	@Test
	void expiryDateMustBeTodayOrLater() {
		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, Set.of(), false, TODAY.minusDays(1), OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, Set.of(), false, null, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
	}

	@Test
	void todayIsTheDateInTheMunicipalityTimeZone() {
		Instant lateEveningUtc = Instant.parse("2026-11-30T23:30:00Z");

		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, Set.of(), false,
				LocalDate.of(2026, 11, 30), OFFICIAL_ID, lateEveningUtc))
				.isInstanceOf(InvalidNoticeException.class);
	}

	@Test
	void neighbourhoodsMustBeAGivenSetWithoutEmptyIds() {
		Set<Long> withNull = new HashSet<>();
		withNull.add(null);

		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, null, false, EXPIRES_ON, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
		assertThatThrownBy(() -> Notice.publish(TITLE, CONTENT, CATEGORY, withNull, false, EXPIRES_ON, OFFICIAL_ID, NOW))
				.isInstanceOf(InvalidNoticeException.class);
	}

	@Test
	void neighbourhoodsAreCopiedNotShared() {
		Set<Long> ids = new HashSet<>(Set.of(NEIGHBOURHOOD_ID));
		Notice notice = publishFor(ids);

		ids.add(OTHER_NEIGHBOURHOOD_ID);

		assertThat(notice.getNeighbourhoodIds()).containsExactly(NEIGHBOURHOOD_ID);
	}

	@Test
	void noticeStaysActiveOnItsExpiryDateAndExpiresTheDayAfter() {
		Notice notice = publishFor(Set.of());

		assertThat(notice.statusOn(EXPIRES_ON)).isEqualTo(ACTIVE);
		assertThat(notice.statusOn(EXPIRES_ON.plusDays(1))).isEqualTo(EXPIRED);
	}

	@Test
	void withdrawnNoticeIsWithdrawnEvenBeforeItsExpiryDate() {
		Notice notice = publishFor(Set.of());

		notice.withdraw(NOW);

		assertThat(notice.getWithdrawnAt()).isEqualTo(NOW);
		assertThat(notice.statusOn(TODAY)).isEqualTo(WITHDRAWN);
		assertThat(notice.statusOn(EXPIRES_ON.plusDays(1))).isEqualTo(WITHDRAWN);
	}

	@Test
	void onlyAnActiveNoticeCanBeWithdrawn() {
		Notice withdrawn = publishFor(Set.of());
		withdrawn.withdraw(NOW);
		Notice expired = publishFor(Set.of());
		Instant afterExpiry = Instant.parse("2026-12-01T09:00:00Z");

		assertThatThrownBy(() -> withdrawn.withdraw(NOW))
				.isInstanceOf(NoticeNotActiveException.class);
		assertThatThrownBy(() -> expired.withdraw(afterExpiry))
				.isInstanceOf(NoticeNotActiveException.class)
				.extracting("status").isEqualTo(EXPIRED);
		assertThat(expired.getWithdrawnAt()).isNull();
	}

	@Test
	void editChangesTheFieldsAndKeepsThePublication() {
		Notice notice = publishFor(Set.of(NEIGHBOURHOOD_ID));

		notice.edit("Water cut moved to Tuesday", "No water from 10:00 to 16:00.", "WATER",
				Set.of(OTHER_NEIGHBOURHOOD_ID), false, TODAY, TODAY);

		assertThat(notice.getTitle()).isEqualTo("Water cut moved to Tuesday");
		assertThat(notice.getContent()).isEqualTo("No water from 10:00 to 16:00.");
		assertThat(notice.getNeighbourhoodIds()).containsExactly(OTHER_NEIGHBOURHOOD_ID);
		assertThat(notice.isUrgent()).isFalse();
		assertThat(notice.getExpiresOn()).isEqualTo(TODAY);
		assertThat(notice.getPublishedBy()).isEqualTo(OFFICIAL_ID);
		assertThat(notice.getPublishedAt()).isEqualTo(NOW);
		assertThat(notice.statusOn(TODAY)).isEqualTo(ACTIVE);
	}

	@Test
	void onlyAnActiveNoticeCanBeEdited() {
		Notice expired = publishFor(Set.of());
		Notice withdrawn = publishFor(Set.of());
		withdrawn.withdraw(NOW);
		LocalDate afterExpiry = EXPIRES_ON.plusDays(1);

		assertThatThrownBy(() -> expired.edit("New title", CONTENT, CATEGORY, Set.of(), false, afterExpiry, afterExpiry))
				.isInstanceOf(NoticeNotActiveException.class);
		assertThatThrownBy(() -> withdrawn.edit("New title", CONTENT, CATEGORY, Set.of(), false, EXPIRES_ON, TODAY))
				.isInstanceOf(NoticeNotActiveException.class)
				.extracting("status").isEqualTo(WITHDRAWN);
		assertThat(expired.getTitle()).isEqualTo(TITLE);
		assertThat(withdrawn.getTitle()).isEqualTo(TITLE);
	}

	@Test
	void invalidEditLeavesTheNoticeUnchanged() {
		Notice notice = publishFor(Set.of(NEIGHBOURHOOD_ID));

		assertThatThrownBy(() -> notice.edit("New title", CONTENT, CATEGORY, Set.of(), false, TODAY.minusDays(1), TODAY))
				.isInstanceOf(InvalidNoticeException.class);
		assertThatThrownBy(() -> notice.edit("New title", " ", CATEGORY, Set.of(), false, EXPIRES_ON, TODAY))
				.isInstanceOf(InvalidNoticeException.class);

		assertThat(notice.getTitle()).isEqualTo(TITLE);
		assertThat(notice.getContent()).isEqualTo(CONTENT);
		assertThat(notice.getNeighbourhoodIds()).containsExactly(NEIGHBOURHOOD_ID);
		assertThat(notice.getExpiresOn()).isEqualTo(EXPIRES_ON);
	}

	@Test
	void wholeMunicipalityNoticeIsVisibleEverywhereAndWithoutANeighbourhood() {
		Notice notice = publishFor(Set.of());

		assertThat(notice.isForWholeMunicipality()).isTrue();
		assertThat(notice.isVisibleIn(NEIGHBOURHOOD_ID, TODAY)).isTrue();
		assertThat(notice.isVisibleIn(null, TODAY)).isTrue();
	}

	@Test
	void neighbourhoodNoticeIsVisibleOnlyInItsNeighbourhoods() {
		Notice notice = publishFor(Set.of(NEIGHBOURHOOD_ID));

		assertThat(notice.isForWholeMunicipality()).isFalse();
		assertThat(notice.isVisibleIn(NEIGHBOURHOOD_ID, TODAY)).isTrue();
		assertThat(notice.isVisibleIn(OTHER_NEIGHBOURHOOD_ID, TODAY)).isFalse();
		assertThat(notice.isVisibleIn(null, TODAY)).isFalse();
	}

	@Test
	void expiredOrWithdrawnNoticeIsNotVisible() {
		Notice expired = publishFor(Set.of());
		Notice withdrawn = publishFor(Set.of());
		withdrawn.withdraw(NOW);

		assertThat(expired.isVisibleIn(null, EXPIRES_ON.plusDays(1))).isFalse();
		assertThat(withdrawn.isVisibleIn(null, TODAY)).isFalse();
	}

	@Test
	void restoredNoticeKeepsItsSavedState() {
		Instant withdrawnAt = Instant.parse("2026-11-24T08:00:00Z");

		Notice notice = Notice.restore(12L, TITLE, CONTENT, CATEGORY, Set.of(NEIGHBOURHOOD_ID), true,
				OFFICIAL_ID, NOW, EXPIRES_ON, withdrawnAt);

		assertThat(notice.getId()).isEqualTo(12L);
		assertThat(notice.getWithdrawnAt()).isEqualTo(withdrawnAt);
		assertThat(notice.statusOn(TODAY)).isEqualTo(WITHDRAWN);
		assertThat(notice.getNeighbourhoodIds()).containsExactly(NEIGHBOURHOOD_ID);
	}

	private static Notice publishFor(Set<Long> neighbourhoodIds) {
		return Notice.publish(TITLE, CONTENT, CATEGORY, neighbourhoodIds, true, EXPIRES_ON, OFFICIAL_ID, NOW);
	}
}
