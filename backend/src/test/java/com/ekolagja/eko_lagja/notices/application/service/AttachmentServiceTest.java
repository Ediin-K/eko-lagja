package com.ekolagja.eko_lagja.notices.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ekolagja.eko_lagja.notices.application.port.in.AttachmentNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.FileContent;
import com.ekolagja.eko_lagja.notices.application.port.in.FileUpload;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.application.port.out.ComplaintAccess;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.FileStore;
import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;
import com.ekolagja.eko_lagja.notices.domain.FileNotAllowedException;
import com.ekolagja.eko_lagja.notices.domain.FileType;
import com.ekolagja.eko_lagja.notices.domain.Notice;
import com.ekolagja.eko_lagja.notices.domain.NoticeNotActiveException;

class AttachmentServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-26T09:00:00Z");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 26);
	private static final Long OFFICIAL_ID = 3L;
	private static final Long RESIDENT_ID = 21L;
	private static final Long COMPLAINT_ID = 42L;
	private static final Long OTHER_COMPLAINT_ID = 43L;

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };
	private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16 };
	private static final byte[] PDF = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] EXE = { 'M', 'Z', (byte) 0x90, 0, 3, 0 };

	private final InMemoryNotices notices = new InMemoryNotices();
	private final InMemoryAttachments attachments = new InMemoryAttachments();
	private final InMemoryFileStore fileStore = new InMemoryFileStore();
	private final FakeComplaintAccess complaintAccess = new FakeComplaintAccess();
	private final FakeCurrentUser currentUser = new FakeCurrentUser();
	private final AttachmentService service = new AttachmentService(attachments, fileStore, notices,
			complaintAccess, currentUser, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void officialAttachesAFileToAnActiveNotice() {
		Long noticeId = notices.seed(TODAY.plusDays(7), null);
		currentUser.logInAsOfficial();

		Long id = service.store(AttachmentOwner.ofNotice(noticeId), new FileUpload("map.png", "image/png", PNG));

		Attachment saved = attachments.byId(id);
		assertThat(saved.getOwner()).isEqualTo(AttachmentOwner.ofNotice(noticeId));
		assertThat(saved.getFileType()).isEqualTo(FileType.PNG);
		assertThat(saved.getSizeBytes()).isEqualTo(PNG.length);
		assertThat(saved.getUploadedAt()).isEqualTo(NOW);
		assertThat(fileStore.bytesAt(saved.getStorageKey())).isEqualTo(PNG);
	}

	@Test
	void residentsAndVisitorsCannotAttachToNotices() {
		Long noticeId = notices.seed(TODAY.plusDays(7), null);
		FileUpload upload = new FileUpload("map.png", "image/png", PNG);

		currentUser.logInAsResident();
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId), upload))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logOut();
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId), upload))
				.isInstanceOf(NotAllowedException.class);

		assertThat(fileStore.count()).isZero();
	}

	@Test
	void missingExpiredOrWithdrawnNoticesTakeNoFiles() {
		Long expired = notices.seed(TODAY.minusDays(1), null);
		Long withdrawn = notices.seed(TODAY.plusDays(7), NOW.minusSeconds(60));
		FileUpload upload = new FileUpload("map.png", "image/png", PNG);
		currentUser.logInAsOfficial();

		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(404L), upload))
				.isInstanceOf(NoticeNotFoundException.class);
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(expired), upload))
				.isInstanceOf(NoticeNotActiveException.class);
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(withdrawn), upload))
				.isInstanceOf(NoticeNotActiveException.class);
		assertThat(fileStore.count()).isZero();
	}

	@Test
	void fileWhoseBytesDoNotMatchItsTypeIsRefused() {
		Long noticeId = notices.seed(TODAY.plusDays(7), null);
		currentUser.logInAsOfficial();

		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId),
				new FileUpload("photo.png", "image/png", EXE)))
				.isInstanceOf(FileNotAllowedException.class);
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId),
				new FileUpload("plan.pdf", "application/pdf", PNG)))
				.isInstanceOf(FileNotAllowedException.class);
		assertThat(fileStore.count()).isZero();
		assertThat(attachments.count()).isZero();
	}

	@Test
	void wrongTypeTooLargeOrEmptyFileIsRefusedBeforeAnythingIsWritten() {
		Long noticeId = notices.seed(TODAY.plusDays(7), null);
		byte[] tooLarge = new byte[(int) Attachment.MAX_SIZE_BYTES + 1];
		System.arraycopy(PDF, 0, tooLarge, 0, PDF.length);
		currentUser.logInAsOfficial();

		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId),
				new FileUpload("setup.exe", "application/x-msdownload", EXE)))
				.isInstanceOf(FileNotAllowedException.class);
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId),
				new FileUpload("big.pdf", "application/pdf", tooLarge)))
				.isInstanceOf(FileNotAllowedException.class);
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofNotice(noticeId),
				new FileUpload("empty.pdf", "application/pdf", new byte[0])))
				.isInstanceOf(FileNotAllowedException.class);
		assertThat(fileStore.count()).isZero();
		assertThat(attachments.count()).isZero();
	}

	@Test
	void complaintPhotoIsStoredWhenTheComplaintsModuleAllowsIt() {
		currentUser.logInAsResident();
		complaintAccess.allowAddPhoto(RESIDENT_ID, COMPLAINT_ID);

		Long id = service.store(AttachmentOwner.ofComplaint(COMPLAINT_ID),
				new FileUpload("pothole.jpg", "image/jpeg", JPEG));

		Attachment saved = attachments.byId(id);
		assertThat(saved.getOwner()).isEqualTo(AttachmentOwner.ofComplaint(COMPLAINT_ID));
		assertThat(saved.getFileType()).isEqualTo(FileType.JPEG);
		assertThat(fileStore.bytesAt(saved.getStorageKey())).isEqualTo(JPEG);
	}

	@Test
	void complaintPhotoIsRefusedWithoutTheComplaintsModulesPermission() {
		FileUpload upload = new FileUpload("pothole.jpg", "image/jpeg", JPEG);
		complaintAccess.allowAddPhoto(RESIDENT_ID, OTHER_COMPLAINT_ID);
		complaintAccess.allowView(RESIDENT_ID, COMPLAINT_ID);

		currentUser.logOut();
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofComplaint(COMPLAINT_ID), upload))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logInAsResident();
		assertThatThrownBy(() -> service.store(AttachmentOwner.ofComplaint(COMPLAINT_ID), upload))
				.isInstanceOf(NotAllowedException.class);

		assertThat(fileStore.count()).isZero();
		assertThat(attachments.count()).isZero();
	}

	@Test
	void anyoneOpensAFileOfAnActiveNotice() {
		Long noticeId = notices.seed(TODAY, null);
		Long id = attachments.seed(AttachmentOwner.ofNotice(noticeId), "plan.pdf", FileType.PDF, PDF, fileStore);
		currentUser.logOut();

		FileContent file = service.open(id);

		assertThat(file.fileName()).isEqualTo("plan.pdf");
		assertThat(file.contentType()).isEqualTo("application/pdf");
		assertThat(file.content()).isEqualTo(PDF);
	}

	@Test
	void filesOfExpiredOrWithdrawnNoticesAreNotFound() {
		Long expired = notices.seed(TODAY.minusDays(1), null);
		Long withdrawn = notices.seed(TODAY.plusDays(7), NOW.minusSeconds(60));
		Long expiredFile = attachments.seed(AttachmentOwner.ofNotice(expired), "a.pdf", FileType.PDF, PDF, fileStore);
		Long withdrawnFile = attachments.seed(AttachmentOwner.ofNotice(withdrawn), "b.pdf", FileType.PDF, PDF, fileStore);

		assertThatThrownBy(() -> service.open(expiredFile)).isInstanceOf(AttachmentNotFoundException.class);
		assertThatThrownBy(() -> service.open(withdrawnFile)).isInstanceOf(AttachmentNotFoundException.class);
		assertThatThrownBy(() -> service.open(404L)).isInstanceOf(AttachmentNotFoundException.class);
	}

	@Test
	void complaintPhotosAreNeverOpenedAsNoticeFiles() {
		Long photo = attachments.seed(AttachmentOwner.ofComplaint(COMPLAINT_ID), "pothole.jpg", FileType.JPEG,
				JPEG, fileStore);
		currentUser.logInAsOfficial();

		assertThatThrownBy(() -> service.open(photo)).isInstanceOf(AttachmentNotFoundException.class);
	}

	@Test
	void complaintPhotoOpensOnlyForItsOwnComplaint() {
		Long photo = attachments.seed(AttachmentOwner.ofComplaint(COMPLAINT_ID), "pothole.jpg", FileType.JPEG,
				JPEG, fileStore);
		Long noticeId = notices.seed(TODAY.plusDays(7), null);
		Long noticeFile = attachments.seed(AttachmentOwner.ofNotice(noticeId), "plan.pdf", FileType.PDF, PDF, fileStore);
		currentUser.logInAsResident();
		complaintAccess.allowView(RESIDENT_ID, COMPLAINT_ID);
		complaintAccess.allowView(RESIDENT_ID, OTHER_COMPLAINT_ID);

		FileContent file = service.openComplaintPhoto(COMPLAINT_ID, photo);

		assertThat(file.contentType()).isEqualTo("image/jpeg");
		assertThat(file.content()).isEqualTo(JPEG);
		assertThatThrownBy(() -> service.openComplaintPhoto(OTHER_COMPLAINT_ID, photo))
				.isInstanceOf(AttachmentNotFoundException.class);
		assertThatThrownBy(() -> service.openComplaintPhoto(COMPLAINT_ID, noticeFile))
				.isInstanceOf(AttachmentNotFoundException.class);
		assertThatThrownBy(() -> service.openComplaintPhoto(COMPLAINT_ID, 404L))
				.isInstanceOf(AttachmentNotFoundException.class);
	}

	@Test
	void complaintPhotoOpensOnlyWhenTheComplaintsModuleAllowsTheCurrentUser() {
		Long photo = attachments.seed(AttachmentOwner.ofComplaint(COMPLAINT_ID), "pothole.jpg", FileType.JPEG,
				JPEG, fileStore);
		complaintAccess.allowView(OFFICIAL_ID, COMPLAINT_ID);
		complaintAccess.allowAddPhoto(RESIDENT_ID, COMPLAINT_ID);

		currentUser.logOut();
		assertThatThrownBy(() -> service.openComplaintPhoto(COMPLAINT_ID, photo))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logInAsResident();
		assertThatThrownBy(() -> service.openComplaintPhoto(COMPLAINT_ID, photo))
				.isInstanceOf(NotAllowedException.class);

		currentUser.logInAsOfficial();
		assertThat(service.openComplaintPhoto(COMPLAINT_ID, photo).content()).isEqualTo(JPEG);
	}

	private record Grant(Long userId, Long complaintId) {
	}

	private static class FakeComplaintAccess implements ComplaintAccess {

		private final Set<Grant> viewers = new HashSet<>();
		private final Set<Grant> photographers = new HashSet<>();

		void allowView(Long userId, Long complaintId) {
			viewers.add(new Grant(userId, complaintId));
		}

		void allowAddPhoto(Long userId, Long complaintId) {
			photographers.add(new Grant(userId, complaintId));
		}

		@Override
		public boolean canView(Long userId, Long complaintId) {
			return viewers.contains(new Grant(userId, complaintId));
		}

		@Override
		public boolean canAddPhoto(Long userId, Long complaintId) {
			return photographers.contains(new Grant(userId, complaintId));
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
			id = RESIDENT_ID;
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

	private static class InMemoryNotices implements Notices {

		private final Map<Long, Notice> byId = new HashMap<>();
		private long nextId = 1;

		Long seed(LocalDate expiresOn, Instant withdrawnAt) {
			Long id = nextId++;
			byId.put(id, Notice.restore(id, "Water cut", "No water.", "WATER", Set.of(), false,
					OFFICIAL_ID, NOW.minusSeconds(3600), expiresOn, withdrawnAt));
			return id;
		}

		@Override
		public Notice save(Notice notice) {
			byId.put(notice.getId(), notice);
			return notice;
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

	private static class InMemoryAttachments implements Attachments {

		private final Map<Long, Attachment> byId = new HashMap<>();
		private long nextId = 1;

		Long seed(AttachmentOwner owner, String fileName, FileType type, byte[] content, InMemoryFileStore store) {
			Long id = nextId++;
			String key = "seed-" + id;
			store.write(key, content);
			byId.put(id, Attachment.restore(id, fileName, type, content.length, NOW, owner, key));
			return id;
		}

		Attachment byId(Long id) {
			return byId.get(id);
		}

		int count() {
			return byId.size();
		}

		@Override
		public Attachment save(Attachment attachment) {
			Attachment stored = attachment.getId() != null ? attachment
					: Attachment.restore(nextId++, attachment.getFileName(), attachment.getFileType(),
							attachment.getSizeBytes(), attachment.getUploadedAt(), attachment.getOwner(),
							attachment.getStorageKey());
			byId.put(stored.getId(), stored);
			return stored;
		}

		@Override
		public Optional<Attachment> findById(Long id) {
			return Optional.ofNullable(byId.get(id));
		}

		@Override
		public List<Attachment> findByOwner(AttachmentOwner owner) {
			return byId.values().stream().filter(attachment -> attachment.getOwner().equals(owner)).toList();
		}
	}

	private static class InMemoryFileStore implements FileStore {

		private final Map<String, byte[]> files = new HashMap<>();

		byte[] bytesAt(String storageKey) {
			return files.get(storageKey);
		}

		int count() {
			return files.size();
		}

		@Override
		public void write(String storageKey, byte[] content) {
			files.put(storageKey, content.clone());
		}

		@Override
		public byte[] read(String storageKey) {
			return files.get(storageKey).clone();
		}
	}
}
