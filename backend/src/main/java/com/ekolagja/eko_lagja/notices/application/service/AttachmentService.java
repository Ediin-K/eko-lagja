package com.ekolagja.eko_lagja.notices.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import com.ekolagja.eko_lagja.notices.application.port.in.AttachmentNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.FileContent;
import com.ekolagja.eko_lagja.notices.application.port.in.FileUpload;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeNotFoundException;
import com.ekolagja.eko_lagja.notices.application.port.in.OpenAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.StoreAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.application.port.out.ComplaintAccess;
import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;
import com.ekolagja.eko_lagja.notices.application.port.out.FileStore;
import com.ekolagja.eko_lagja.notices.application.port.out.Notices;
import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;
import com.ekolagja.eko_lagja.notices.domain.FileNotAllowedException;
import com.ekolagja.eko_lagja.notices.domain.FileProblem;
import com.ekolagja.eko_lagja.notices.domain.FileType;
import com.ekolagja.eko_lagja.notices.domain.Notice;
import com.ekolagja.eko_lagja.notices.domain.NoticeNotActiveException;
import com.ekolagja.eko_lagja.notices.domain.NoticeStatus;

public class AttachmentService implements StoreAttachmentUseCase, OpenAttachmentUseCase {

	private final Attachments attachments;
	private final FileStore fileStore;
	private final Notices notices;
	private final ComplaintAccess complaintAccess;
	private final CurrentUser currentUser;
	private final Clock clock;

	public AttachmentService(Attachments attachments, FileStore fileStore, Notices notices,
			ComplaintAccess complaintAccess, CurrentUser currentUser, Clock clock) {
		this.attachments = attachments;
		this.fileStore = fileStore;
		this.notices = notices;
		this.complaintAccess = complaintAccess;
		this.currentUser = currentUser;
		this.clock = clock;
	}

	@Override
	public Long store(AttachmentOwner owner, FileUpload upload) {
		switch (owner.type()) {
			case NOTICE -> requireOfficialAndActiveNotice(owner.id());
			case COMPLAINT -> {
				if (!complaintAccess.canAddPhoto(requireLoggedIn(), owner.id())) {
					throw new NotAllowedException("not allowed to add photos to this complaint");
				}
			}
		}
		byte[] content = upload.content() == null ? new byte[0] : upload.content();
		Attachment attachment = Attachment.create(upload.fileName(), upload.contentType(), content.length,
				owner, UUID.randomUUID().toString(), Instant.now(clock));
		if (!contentMatches(attachment.getFileType(), content)) {
			throw new FileNotAllowedException(FileProblem.CONTENT_MISMATCH, "file content does not match its type");
		}
		fileStore.write(attachment.getStorageKey(), content);
		return attachments.save(attachment).getId();
	}

	@Override
	public FileContent open(Long attachmentId) {
		Attachment attachment = attachments.findById(attachmentId)
				.filter(Attachment::belongsToNotice)
				.filter(found -> isActiveNotice(found.getOwner().id()))
				.orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
		return read(attachment);
	}

	@Override
	public FileContent openComplaintPhoto(Long complaintId, Long attachmentId) {
		if (!complaintAccess.canView(requireLoggedIn(), complaintId)) {
			throw new NotAllowedException("not allowed to view this complaint");
		}
		AttachmentOwner complaint = AttachmentOwner.ofComplaint(complaintId);
		Attachment attachment = attachments.findById(attachmentId)
				.filter(found -> found.getOwner().equals(complaint))
				.orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
		return read(attachment);
	}

	private void requireOfficialAndActiveNotice(Long noticeId) {
		requireOfficial();
		Notice notice = notices.findById(noticeId).orElseThrow(() -> new NoticeNotFoundException(noticeId));
		NoticeStatus status = notice.statusOn(today());
		if (status != NoticeStatus.ACTIVE) {
			throw new NoticeNotActiveException(status);
		}
	}

	private boolean isActiveNotice(Long noticeId) {
		return notices.findById(noticeId)
				.map(notice -> notice.statusOn(today()) == NoticeStatus.ACTIVE)
				.orElse(false);
	}

	private FileContent read(Attachment attachment) {
		return new FileContent(attachment.getFileName(), attachment.getFileType().getContentType(),
				fileStore.read(attachment.getStorageKey()));
	}

	private static boolean contentMatches(FileType type, byte[] content) {
		byte[] signature = switch (type) {
			case JPEG -> new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
			case PNG -> new byte[] { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };
			case PDF -> new byte[] { '%', 'P', 'D', 'F', '-' };
		};
		return content.length >= signature.length
				&& Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
	}

	private void requireOfficial() {
		requireLoggedIn();
		if (!currentUser.isOfficial()) {
			throw new NotAllowedException("only municipal officials can attach files to notices");
		}
	}

	private Long requireLoggedIn() {
		return currentUser.id().orElseThrow(() -> new NotAllowedException("no logged-in user"));
	}

	private LocalDate today() {
		return LocalDate.ofInstant(Instant.now(clock), Notice.MUNICIPALITY_ZONE);
	}
}
