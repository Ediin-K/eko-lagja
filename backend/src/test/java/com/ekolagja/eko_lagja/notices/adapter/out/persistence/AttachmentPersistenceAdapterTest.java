package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;
import com.ekolagja.eko_lagja.notices.domain.FileType;

@DataJpaTest
@Import(AttachmentPersistenceAdapter.class)
class AttachmentPersistenceAdapterTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final AttachmentOwner NOTICE = AttachmentOwner.ofNotice(12L);
	private static final AttachmentOwner OTHER_NOTICE = AttachmentOwner.ofNotice(13L);
	private static final AttachmentOwner COMPLAINT = AttachmentOwner.ofComplaint(12L);

	@Autowired
	private Attachments attachments;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void savedAttachmentReadsBackWithEveryField() {
		Long id = attachments.save(attachment("plan.pdf", "application/pdf", NOTICE, "key-1")).getId();
		clearCache();

		Attachment found = attachments.findById(id).orElseThrow();

		assertThat(found.getFileName()).isEqualTo("plan.pdf");
		assertThat(found.getFileType()).isEqualTo(FileType.PDF);
		assertThat(found.getSizeBytes()).isEqualTo(2048);
		assertThat(found.getUploadedAt()).isEqualTo(NOW);
		assertThat(found.getOwner()).isEqualTo(NOTICE);
		assertThat(found.getStorageKey()).isEqualTo("key-1");
	}

	@Test
	void filesAreFoundByTheirExactOwner() {
		Long first = attachments.save(attachment("plan.pdf", "application/pdf", NOTICE, "key-1")).getId();
		Long second = attachments.save(attachment("map.png", "image/png", NOTICE, "key-2")).getId();
		attachments.save(attachment("other.png", "image/png", OTHER_NOTICE, "key-3"));
		attachments.save(attachment("pothole.jpg", "image/jpeg", COMPLAINT, "key-4"));
		clearCache();

		assertThat(attachments.findByOwner(NOTICE)).extracting(Attachment::getId).containsExactly(first, second);
		assertThat(attachments.findByOwner(COMPLAINT)).extracting(Attachment::getFileName)
				.containsExactly("pothole.jpg");
	}

	@Test
	void savedAttachmentNeverChanges() {
		Attachment saved = attachments.save(attachment("plan.pdf", "application/pdf", NOTICE, "key-1"));

		assertThatIllegalArgumentException().isThrownBy(() -> attachments.save(saved));
	}

	private static Attachment attachment(String fileName, String contentType, AttachmentOwner owner, String key) {
		return Attachment.create(fileName, contentType, 2048, owner, key, NOW);
	}

	private void clearCache() {
		entityManager.flush();
		entityManager.clear();
	}
}
