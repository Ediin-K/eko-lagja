package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.domain.Notice;

@DataJpaTest
@Import(NoticePersistenceAdapter.class)
class NoticePersistenceAdapterTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 26);
	private static final Long OFFICIAL_ID = 3L;
	private static final Long CENTER = 5L;
	private static final Long SUNNY_HILL = 9L;

	@Autowired
	private Notices notices;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void newNoticeIsSavedWithEveryField() {
		Long id = notices.save(publish(Set.of(CENTER, SUNNY_HILL), true, TODAY.plusDays(7))).getId();
		clearCache();

		Notice found = notices.findById(id).orElseThrow();

		assertThat(found.getTitle()).isEqualTo("Water cut");
		assertThat(found.getContent()).isEqualTo("No water from 08:00 to 14:00.");
		assertThat(found.getCategory()).isEqualTo("WATER");
		assertThat(found.getNeighbourhoodIds()).containsExactlyInAnyOrder(CENTER, SUNNY_HILL);
		assertThat(found.isUrgent()).isTrue();
		assertThat(found.getPublishedBy()).isEqualTo(OFFICIAL_ID);
		assertThat(found.getPublishedAt()).isEqualTo(NOW);
		assertThat(found.getExpiresOn()).isEqualTo(TODAY.plusDays(7));
		assertThat(found.getWithdrawnAt()).isNull();
	}

	@Test
	void editAndWithdrawAreSaved() {
		Notice notice = notices.save(publish(Set.of(CENTER), false, TODAY.plusDays(7)));
		notice.edit("Water cut moved", "Now on Tuesday.", "ROADS", Set.of(SUNNY_HILL), true, TODAY, TODAY);
		notices.save(notice);
		clearCache();

		Notice edited = notices.findById(notice.getId()).orElseThrow();
		assertThat(edited.getTitle()).isEqualTo("Water cut moved");
		assertThat(edited.getCategory()).isEqualTo("ROADS");
		assertThat(edited.getNeighbourhoodIds()).containsExactly(SUNNY_HILL);
		assertThat(edited.isUrgent()).isTrue();
		assertThat(edited.getExpiresOn()).isEqualTo(TODAY);

		edited.withdraw(NOW.plusSeconds(60));
		notices.save(edited);
		clearCache();

		assertThat(notices.findById(notice.getId()).orElseThrow().getWithdrawnAt()).isEqualTo(NOW.plusSeconds(60));
	}

	@Test
	void activeNoticesAreFoundByNeighbourhood() {
		LocalDate later = TODAY.plusDays(5);
		Long whole = notices.save(publish(Set.of(), false, TODAY.plusDays(10))).getId();
		Long center = notices.save(publish(Set.of(CENTER), false, TODAY.plusDays(10))).getId();
		Long both = notices.save(publish(Set.of(CENTER, SUNNY_HILL), false, later)).getId();
		notices.save(publish(Set.of(SUNNY_HILL), false, TODAY.plusDays(10)));
		notices.save(publish(Set.of(CENTER), false, TODAY.plusDays(4)));
		Notice withdrawn = notices.save(publish(Set.of(CENTER), false, TODAY.plusDays(10)));
		withdrawn.withdraw(NOW);
		notices.save(withdrawn);
		clearCache();

		assertThat(notices.findActiveFor(CENTER, later)).extracting(Notice::getId)
				.containsExactlyInAnyOrder(whole, center, both);
		assertThat(notices.findActiveFor(null, later)).extracting(Notice::getId)
				.containsExactly(whole);
	}

	@Test
	void savingANoticeThatWasNeverStoredFails() {
		Notice unknown = Notice.restore(999L, "Water cut", "No water.", "WATER", Set.of(), false,
				OFFICIAL_ID, NOW, TODAY, null);

		assertThatIllegalArgumentException().isThrownBy(() -> notices.save(unknown));
	}

	private static Notice publish(Set<Long> neighbourhoodIds, boolean urgent, LocalDate expiresOn) {
		return Notice.publish("Water cut", "No water from 08:00 to 14:00.", "WATER", neighbourhoodIds, urgent,
				expiresOn, OFFICIAL_ID, NOW);
	}

	private void clearCache() {
		entityManager.flush();
		entityManager.clear();
	}
}
