package com.ekolagja.eko_lagja.notices.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AttachmentTest {

	private static final String FILE_NAME = "water-cut-map.pdf";
	private static final String STORAGE_KEY = "2026/11/0f3a9c.pdf";
	private static final AttachmentOwner NOTICE = AttachmentOwner.ofNotice(12L);
	private static final AttachmentOwner COMPLAINT = AttachmentOwner.ofComplaint(42L);
	private static final Instant NOW = Instant.parse("2026-11-23T09:00:00Z");

	@Test
	void newAttachmentKeepsItsData() {
		Attachment attachment = Attachment.create(FILE_NAME, "application/pdf", 2_048, NOTICE, STORAGE_KEY, NOW);

		assertThat(attachment.getId()).isNull();
		assertThat(attachment.getFileName()).isEqualTo(FILE_NAME);
		assertThat(attachment.getFileType()).isEqualTo(FileType.PDF);
		assertThat(attachment.getSizeBytes()).isEqualTo(2_048);
		assertThat(attachment.getUploadedAt()).isEqualTo(NOW);
		assertThat(attachment.getOwner()).isEqualTo(NOTICE);
		assertThat(attachment.getStorageKey()).isEqualTo(STORAGE_KEY);
	}

	@ParameterizedTest
	@CsvSource({
			"image/jpeg, JPEG",
			"image/png, PNG",
			"application/pdf, PDF",
			"IMAGE/JPEG, JPEG",
			"'application/pdf; charset=binary', PDF"})
	void allowedContentTypesAreRecognised(String contentType, FileType expected) {
		assertThat(FileType.fromContentType(contentType)).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {"image/gif", "text/html", "application/zip", "image/svg+xml", ""})
	void otherContentTypesAreRefused(String contentType) {
		assertThatThrownBy(() -> Attachment.create("file", contentType, 100, NOTICE, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class);
	}

	@Test
	void missingContentTypeIsRefused() {
		assertThatThrownBy(() -> Attachment.create(FILE_NAME, null, 100, NOTICE, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class);
	}

	@Test
	void fileOfExactlyTenMegabytesIsAllowed() {
		Attachment attachment = Attachment.create("photo.png", "image/png", Attachment.MAX_SIZE_BYTES, COMPLAINT, STORAGE_KEY, NOW);

		assertThat(attachment.getSizeBytes()).isEqualTo(10L * 1024 * 1024);
	}

	@Test
	void fileLargerThanTenMegabytesIsRefused() {
		assertThatThrownBy(() -> Attachment.create("photo.png", "image/png", Attachment.MAX_SIZE_BYTES + 1, COMPLAINT, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class);
	}

	@Test
	void emptyFileIsRefused() {
		assertThatThrownBy(() -> Attachment.create("photo.png", "image/png", 0, COMPLAINT, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class);
	}

	@Test
	void fileNameHasAMaximumLength() {
		Attachment longest = Attachment.create("a".repeat(251) + ".pdf", "application/pdf", 100, NOTICE, STORAGE_KEY, NOW);
		assertThat(longest.getFileName()).hasSize(255);

		assertThatThrownBy(() -> Attachment.create("a".repeat(252) + ".pdf", "application/pdf", 100, NOTICE, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class).extracting("reason").isEqualTo(FileProblem.NAME_TOO_LONG);
	}

	@Test
	void everyRefusalNamesItsReason() {
		assertThatThrownBy(() -> Attachment.create(" ", "image/png", 100, NOTICE, STORAGE_KEY, NOW))
				.extracting("reason").isEqualTo(FileProblem.NAME_MISSING);
		assertThatThrownBy(() -> Attachment.create(FILE_NAME, "application/zip", 100, NOTICE, STORAGE_KEY, NOW))
				.extracting("reason").isEqualTo(FileProblem.TYPE_NOT_ALLOWED);
		assertThatThrownBy(() -> Attachment.create(FILE_NAME, null, 100, NOTICE, STORAGE_KEY, NOW))
				.extracting("reason").isEqualTo(FileProblem.TYPE_NOT_ALLOWED);
		assertThatThrownBy(() -> Attachment.create(FILE_NAME, "application/pdf", 0, NOTICE, STORAGE_KEY, NOW))
				.extracting("reason").isEqualTo(FileProblem.EMPTY);
		assertThatThrownBy(() -> Attachment.create(FILE_NAME, "application/pdf", Attachment.MAX_SIZE_BYTES + 1, NOTICE,
				STORAGE_KEY, NOW)).extracting("reason").isEqualTo(FileProblem.TOO_LARGE);
	}

	@Test
	void fileNameIsRequired() {
		assertThatThrownBy(() -> Attachment.create(" ", "image/png", 100, NOTICE, STORAGE_KEY, NOW))
				.isInstanceOf(FileNotAllowedException.class);
	}

	@Test
	void ownerStorageKeyAndTimeAreRequired() {
		assertThatNullPointerException().isThrownBy(() -> Attachment.create(FILE_NAME, "application/pdf", 100, null, STORAGE_KEY, NOW));
		assertThatNullPointerException().isThrownBy(() -> Attachment.create(FILE_NAME, "application/pdf", 100, NOTICE, STORAGE_KEY, null));
		assertThatIllegalArgumentException().isThrownBy(() -> Attachment.create(FILE_NAME, "application/pdf", 100, NOTICE, "", NOW));
	}

	@Test
	void attachmentKnowsWhetherItBelongsToANotice() {
		Attachment noticeFile = Attachment.create(FILE_NAME, "application/pdf", 100, NOTICE, STORAGE_KEY, NOW);
		Attachment complaintPhoto = Attachment.create("pothole.jpg", "image/jpeg", 100, COMPLAINT, STORAGE_KEY, NOW);

		assertThat(noticeFile.belongsToNotice()).isTrue();
		assertThat(complaintPhoto.belongsToNotice()).isFalse();
	}

	@Test
	void restoredAttachmentKeepsItsSavedState() {
		Attachment attachment = Attachment.restore(5L, FILE_NAME, FileType.PDF, 2048, NOW, NOTICE, STORAGE_KEY);

		assertThat(attachment.getId()).isEqualTo(5L);
		assertThat(attachment.getFileType()).isEqualTo(FileType.PDF);
		assertThat(attachment.getOwner()).isEqualTo(NOTICE);
		assertThat(attachment.getStorageKey()).isEqualTo(STORAGE_KEY);
	}

	@Test
	void ownerNeedsATypeAndAnId() {
		assertThat(AttachmentOwner.ofComplaint(42L)).isEqualTo(new AttachmentOwner(OwnerType.COMPLAINT, 42L));
		assertThatNullPointerException().isThrownBy(() -> AttachmentOwner.ofNotice(null));
		assertThatNullPointerException().isThrownBy(() -> new AttachmentOwner(null, 1L));
	}
}
