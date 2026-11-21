package com.ekolagja.eko_lagja.complaints.domain;

import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.IN_PROGRESS;
import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.RECEIVED;
import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.RESOLVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class DomainValidationTest {

	private static final Instant NOW = Instant.parse("2026-11-19T09:00:00Z");

	@Test
	void statusChangeNeedsBothStatusesAndTime() {
		assertThatNullPointerException().isThrownBy(() -> new StatusChange(null, IN_PROGRESS, NOW, null));
		assertThatNullPointerException().isThrownBy(() -> new StatusChange(RECEIVED, null, NOW, null));
		assertThatNullPointerException().isThrownBy(() -> new StatusChange(RECEIVED, IN_PROGRESS, null, null));
	}

	@Test
	void statusChangeStoresBlankNoteAsNoNote() {
		assertThat(new StatusChange(RECEIVED, IN_PROGRESS, NOW, "   ").note()).isNull();
	}

	@Test
	void statusChangeRemovesSpacesAroundNote() {
		assertThat(new StatusChange(RESOLVED, IN_PROGRESS, NOW, "  Still broken \n").note())
				.isEqualTo("Still broken");
	}

	@Test
	void categoryNeedsNameDescriptionAndDepartment() {
		assertThatIllegalArgumentException().isThrownBy(() -> new ComplaintCategory(1L, " ", "Potholes", 3L));
		assertThatIllegalArgumentException().isThrownBy(() -> new ComplaintCategory(1L, "Roads", null, 3L));
		assertThatNullPointerException().isThrownBy(() -> new ComplaintCategory(1L, "Roads", "Potholes", null));
	}

	@Test
	void newCategoryHasNoIdYet() {
		ComplaintCategory category = new ComplaintCategory(null, "Roads", "Potholes and broken pavements", 3L);

		assertThat(category.id()).isNull();
	}

	@Test
	void officialResponseNeedsTextAndTime() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OfficialResponse("", NOW));
		assertThatNullPointerException().isThrownBy(() -> new OfficialResponse("Repaired on Monday.", null));
	}

	@Test
	void internalNoteNeedsTextTimeAndAuthor() {
		assertThatIllegalArgumentException().isThrownBy(() -> new InternalNote(null, NOW, 4L));
		assertThatNullPointerException().isThrownBy(() -> new InternalNote("Call the contractor", null, 4L));
		assertThatNullPointerException().isThrownBy(() -> new InternalNote("Call the contractor", NOW, null));
	}
}
