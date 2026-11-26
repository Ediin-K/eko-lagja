package com.ekolagja.eko_lagja.notices.adapter.in.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ekolagja.eko_lagja.notices.application.port.in.EditNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.in.EditNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.FileContent;
import com.ekolagja.eko_lagja.notices.application.port.in.FileUpload;
import com.ekolagja.eko_lagja.notices.application.port.in.GetNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.ListNoticesUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeDetails;
import com.ekolagja.eko_lagja.notices.application.port.in.NoticeView;
import com.ekolagja.eko_lagja.notices.application.port.in.OpenAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.PublishNoticeCommand;
import com.ekolagja.eko_lagja.notices.application.port.in.PublishNoticeUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.StoreAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.WithdrawNoticeUseCase;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;

@RestController
@RequestMapping("/api/notices")
class NoticeController {

	private final PublishNoticeUseCase publishNotice;
	private final EditNoticeUseCase editNotice;
	private final WithdrawNoticeUseCase withdrawNotice;
	private final ListNoticesUseCase listNotices;
	private final GetNoticeUseCase getNotice;
	private final StoreAttachmentUseCase storeAttachment;
	private final OpenAttachmentUseCase openAttachment;

	NoticeController(PublishNoticeUseCase publishNotice, EditNoticeUseCase editNotice,
			WithdrawNoticeUseCase withdrawNotice, ListNoticesUseCase listNotices, GetNoticeUseCase getNotice,
			StoreAttachmentUseCase storeAttachment, OpenAttachmentUseCase openAttachment) {
		this.publishNotice = publishNotice;
		this.editNotice = editNotice;
		this.withdrawNotice = withdrawNotice;
		this.listNotices = listNotices;
		this.getNotice = getNotice;
		this.storeAttachment = storeAttachment;
		this.openAttachment = openAttachment;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	CreatedId publish(@RequestBody NoticeRequest request) {
		return new CreatedId(publishNotice.publish(new PublishNoticeCommand(request.title(), request.content(),
				request.category(), request.neighbourhoodIdsOrEmpty(), request.urgent(), request.expiresOn())));
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void edit(@PathVariable Long id, @RequestBody NoticeRequest request) {
		editNotice.edit(new EditNoticeCommand(id, request.title(), request.content(), request.category(),
				request.neighbourhoodIdsOrEmpty(), request.urgent(), request.expiresOn()));
	}

	@PostMapping("/{id}/withdraw")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void withdraw(@PathVariable Long id) {
		withdrawNotice.withdraw(id);
	}

	@GetMapping
	List<NoticeView> list(@RequestParam(required = false) Long neighbourhoodId) {
		return listNotices.list(neighbourhoodId);
	}

	@GetMapping("/{id}")
	NoticeDetails get(@PathVariable Long id) {
		return getNotice.get(id);
	}

	@PostMapping(path = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	CreatedId attach(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
		FileUpload upload = new FileUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes());
		return new CreatedId(storeAttachment.store(AttachmentOwner.ofNotice(id), upload));
	}

	@GetMapping("/files/{fileId}")
	ResponseEntity<byte[]> open(@PathVariable Long fileId) {
		FileContent file = openAttachment.open(fileId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.inline().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
				.header("X-Content-Type-Options", "nosniff")
				.body(file.content());
	}
}
