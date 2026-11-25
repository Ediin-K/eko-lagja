package com.ekolagja.eko_lagja.complaints.domain;

import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.IN_PROGRESS;
import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.RECEIVED;
import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.REJECTED;
import static com.ekolagja.eko_lagja.complaints.domain.ComplaintStatus.RESOLVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ComplaintTest {

	private static final String TITLE = "Pothole on Rruga B";
	private static final String DESCRIPTION = "A deep pothole in front of number 12.";
	private static final String ADDRESS = "Rruga B 12";
	private static final Double LATITUDE = 42.6629;
	private static final Double LONGITUDE = 21.1655;
	private static final Long RESIDENT_ID = 7L;
	private static final Long CATEGORY_ID = 2L;
	private static final Long NEIGHBOURHOOD_ID = 5L;
	private static final Instant SUBMITTED_AT = Instant.parse("2026-11-19T09:00:00Z");
	private static final Instant SETUP_AT = Instant.parse("2026-11-19T12:00:00Z");
	private static final Instant CHANGED_AT = Instant.parse("2026-11-20T10:30:00Z");

	@Test
	void newComplaintIsReceivedWithEmptyHistory() {
		Complaint complaint = newComplaint();

		assertThat(complaint.getStatus()).isEqualTo(RECEIVED);
		assertThat(complaint.getStatusChanges()).isEmpty();
		assertThat(complaint.getId()).isNull();
		assertThat(complaint.getTitle()).isEqualTo(TITLE);
		assertThat(complaint.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(complaint.getAddress()).isEqualTo(ADDRESS);
		assertThat(complaint.getLatitude()).isEqualTo(LATITUDE);
		assertThat(complaint.getLongitude()).isEqualTo(LONGITUDE);
		assertThat(complaint.getSubmittedAt()).isEqualTo(SUBMITTED_AT);
		assertThat(complaint.getResidentId()).isEqualTo(RESIDENT_ID);
		assertThat(complaint.getCategoryId()).isEqualTo(CATEGORY_ID);
		assertThat(complaint.getNeighbourhoodId()).isEqualTo(NEIGHBOURHOOD_ID);
	}

	@Test
	void titleDescriptionAndAddressMustNotBeBlank() {
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				" ", DESCRIPTION, ADDRESS, null, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				TITLE, null, ADDRESS, null, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, "", null, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
	}

	@Test
	void residentCategoryNeighbourhoodAndTimeAreRequired() {
		assertThatNullPointerException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, null, null, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
		assertThatNullPointerException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, null, RESIDENT_ID, null, NEIGHBOURHOOD_ID, SUBMITTED_AT));
		assertThatNullPointerException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, null, RESIDENT_ID, CATEGORY_ID, null, SUBMITTED_AT));
		assertThatNullPointerException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, null));
	}

	@Test
	void mapPinIsOptional() {
		Complaint complaint = Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT);

		assertThat(complaint.getLatitude()).isNull();
		assertThat(complaint.getLongitude()).isNull();
	}

	@Test
	void latitudeAndLongitudeComeTogether() {
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, LATITUDE, null, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, null, LONGITUDE, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
	}

	@ParameterizedTest(name = "latitude {0}, longitude {1}")
	@CsvSource({
		"90.1,  21.1655",
		"-90.1, 21.1655",
		"42.66, 180.1",
		"42.66, -180.1",
		"NaN,   21.1655"
	})
	void mapPinMustBeOnTheMap(Double latitude, Double longitude) {
		assertThatIllegalArgumentException().isThrownBy(() -> Complaint.submit(
				TITLE, DESCRIPTION, ADDRESS, latitude, longitude, RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT));
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"RECEIVED,    IN_PROGRESS",
		"RECEIVED,    REJECTED",
		"IN_PROGRESS, RESOLVED",
		"IN_PROGRESS, REJECTED",
		"RESOLVED,    IN_PROGRESS",
		"REJECTED,    IN_PROGRESS"
	})
	void allowedChangeUpdatesStatusAndRecordsIt(ComplaintStatus from, ComplaintStatus to) {
		Complaint complaint = complaintIn(from);
		int historySizeBefore = complaint.getStatusChanges().size();

		StatusChange change = complaint.changeStatus(to, "Reason", CHANGED_AT);

		assertThat(complaint.getStatus()).isEqualTo(to);
		assertThat(change).isEqualTo(new StatusChange(from, to, CHANGED_AT, "Reason"));
		assertThat(complaint.getStatusChanges())
				.hasSize(historySizeBefore + 1)
				.endsWith(change);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"RECEIVED,    RECEIVED",
		"RECEIVED,    RESOLVED",
		"IN_PROGRESS, RECEIVED",
		"IN_PROGRESS, IN_PROGRESS",
		"RESOLVED,    RECEIVED",
		"RESOLVED,    RESOLVED",
		"RESOLVED,    REJECTED",
		"REJECTED,    RECEIVED",
		"REJECTED,    RESOLVED",
		"REJECTED,    REJECTED"
	})
	void forbiddenChangeIsRefusedAndChangesNothing(ComplaintStatus from, ComplaintStatus to) {
		Complaint complaint = complaintIn(from);
		List<StatusChange> historyBefore = complaint.getStatusChanges();

		assertThatThrownBy(() -> complaint.changeStatus(to, "Reason", CHANGED_AT))
				.isInstanceOf(StatusChangeNotAllowedException.class)
				.hasFieldOrPropertyWithValue("fromStatus", from)
				.hasFieldOrPropertyWithValue("toStatus", to);

		assertThat(complaint.getStatus()).isEqualTo(from);
		assertThat(complaint.getStatusChanges()).isEqualTo(historyBefore);
	}

	@ParameterizedTest(name = "{0} -> IN_PROGRESS with note [{1}]")
	@CsvSource(value = {
		"RESOLVED, NULL",
		"RESOLVED, ''",
		"RESOLVED, '   '",
		"REJECTED, NULL",
		"REJECTED, ''",
		"REJECTED, '   '"
	}, nullValues = "NULL")
	void reopeningWithoutNoteIsRefusedAndChangesNothing(ComplaintStatus closed, String note) {
		Complaint complaint = complaintIn(closed);
		List<StatusChange> historyBefore = complaint.getStatusChanges();

		assertThatThrownBy(() -> complaint.changeStatus(IN_PROGRESS, note, CHANGED_AT))
				.isInstanceOf(ReopenNoteRequiredException.class)
				.hasFieldOrPropertyWithValue("fromStatus", closed);

		assertThat(complaint.getStatus()).isEqualTo(closed);
		assertThat(complaint.getStatusChanges()).isEqualTo(historyBefore);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"RECEIVED,    IN_PROGRESS",
		"RECEIVED,    REJECTED",
		"IN_PROGRESS, RESOLVED",
		"IN_PROGRESS, REJECTED"
	})
	void changesOtherThanReopeningNeedNoNote(ComplaintStatus from, ComplaintStatus to) {
		Complaint complaint = complaintIn(from);

		StatusChange change = complaint.changeStatus(to, null, CHANGED_AT);

		assertThat(complaint.getStatus()).isEqualTo(to);
		assertThat(change.note()).isNull();
	}

	@Test
	void historyKeepsEveryChangeInOrder() {
		Complaint complaint = newComplaint();
		Instant first = Instant.parse("2026-11-19T10:00:00Z");
		Instant second = Instant.parse("2026-11-20T10:00:00Z");
		Instant third = Instant.parse("2026-11-21T10:00:00Z");
		Instant fourth = Instant.parse("2026-11-22T10:00:00Z");

		complaint.changeStatus(IN_PROGRESS, null, first);
		complaint.changeStatus(RESOLVED, "Pothole filled", second);
		complaint.changeStatus(IN_PROGRESS, "Closed by mistake, the hole is still there", third);
		complaint.changeStatus(RESOLVED, null, fourth);

		assertThat(complaint.getStatusChanges()).containsExactly(
				new StatusChange(RECEIVED, IN_PROGRESS, first, null),
				new StatusChange(IN_PROGRESS, RESOLVED, second, "Pothole filled"),
				new StatusChange(RESOLVED, IN_PROGRESS, third, "Closed by mistake, the hole is still there"),
				new StatusChange(IN_PROGRESS, RESOLVED, fourth, null));
	}

	@Test
	void historyCannotBeChangedFromOutside() {
		Complaint complaint = newComplaint();
		StatusChange fake = new StatusChange(RECEIVED, RESOLVED, CHANGED_AT, null);

		assertThatThrownBy(() -> complaint.getStatusChanges().add(fake))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThat(complaint.getStatusChanges()).isEmpty();
	}

	private static Complaint newComplaint() {
		return Complaint.submit(TITLE, DESCRIPTION, ADDRESS, LATITUDE, LONGITUDE,
				RESIDENT_ID, CATEGORY_ID, NEIGHBOURHOOD_ID, SUBMITTED_AT);
	}

	private static Complaint complaintIn(ComplaintStatus status) {
		Complaint complaint = newComplaint();
		switch (status) {
			case RECEIVED -> { }
			case IN_PROGRESS -> complaint.changeStatus(IN_PROGRESS, null, SETUP_AT);
			case RESOLVED -> {
				complaint.changeStatus(IN_PROGRESS, null, SETUP_AT);
				complaint.changeStatus(RESOLVED, null, SETUP_AT);
			}
			case REJECTED -> complaint.changeStatus(REJECTED, null, SETUP_AT);
		}
		return complaint;
	}
}
