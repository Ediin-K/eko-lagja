package com.ekolagja.eko_lagja.notices.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ekolagja.eko_lagja.notices.application.port.in.FileUpload;
import com.ekolagja.eko_lagja.notices.application.port.in.NotAllowedException;
import com.ekolagja.eko_lagja.notices.application.port.in.OpenAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.in.StoreAttachmentUseCase;
import com.ekolagja.eko_lagja.notices.application.port.out.Attachments;
import com.ekolagja.eko_lagja.notices.application.port.out.ComplaintAccess;
import com.ekolagja.eko_lagja.notices.application.port.out.FileStore;
import com.ekolagja.eko_lagja.notices.domain.Attachment;
import com.ekolagja.eko_lagja.notices.domain.AttachmentOwner;
import com.ekolagja.eko_lagja.notices.domain.Notice;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class NoticeControllerTest {

	private static final LocalDate NEXT_WEEK = LocalDate.now(Notice.MUNICIPALITY_ZONE).plusDays(7);
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };
	private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16 };
	private static final byte[] EXE = { 'M', 'Z', (byte) 0x90, 0, 3, 0 };
	private static final HttpHeaders OFFICIAL = user(3, "OFFICIAL");
	private static final HttpHeaders RESIDENT = user(21, "RESIDENT");

	@TempDir
	static Path filesFolder;

	@DynamicPropertySource
	static void filesFolder(DynamicPropertyRegistry registry) {
		registry.add("app.notices.files-dir", () -> filesFolder.toString());
	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private Attachments attachments;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private ComplaintAccess complaintAccess;

	@Autowired
	private OpenAttachmentUseCase openAttachment;

	@Autowired
	private StoreAttachmentUseCase storeAttachment;

	@Test
	void officialPublishesANoticeThatEveryoneCanRead() throws Exception {
		Long id = publish();

		mvc.perform(get("/api/notices"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id))
				.andExpect(jsonPath("$[0].title").value("Water cut"));
		mvc.perform(get("/api/notices/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.notice.title").value("Water cut"))
				.andExpect(jsonPath("$.notice.expiresOn").value(NEXT_WEEK.toString()))
				.andExpect(jsonPath("$.files").isEmpty());
	}

	@Test
	void residentsAndVisitorsCannotPublish() throws Exception {
		mvc.perform(post("/api/notices").headers(RESIDENT).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("Water cut")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("NOT_ALLOWED"));
		mvc.perform(post("/api/notices").contentType(MediaType.APPLICATION_JSON).content(noticeJson("Water cut")))
				.andExpect(status().isForbidden());
	}

	@Test
	void blankTitleIsRefusedWithACode() throws Exception {
		mvc.perform(post("/api/notices").headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson(" ")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_NOTICE"))
				.andExpect(jsonPath("$.field").value("title"));
	}

	@Test
	void tooLongInputGetsAClearCodeInsteadOfAServerError() throws Exception {
		mvc.perform(post("/api/notices").headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("t".repeat(301))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_NOTICE"))
				.andExpect(jsonPath("$.field").value("title"));
		mvc.perform(post("/api/notices").headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("Water cut", "c".repeat(8001))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.field").value("content"));

		Long id = publish();
		upload(id, OFFICIAL, "a".repeat(252) + ".png", "image/png", PNG)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("FILE_NOT_ALLOWED"))
				.andExpect(jsonPath("$.reason").value("NAME_TOO_LONG"));
		mvc.perform(get("/api/notices/{id}", id)).andExpect(jsonPath("$.files").isEmpty());
	}

	@Test
	void officialEditsANotice() throws Exception {
		Long id = publish();

		mvc.perform(put("/api/notices/{id}", id).headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("Water cut moved")))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/notices/{id}", id))
				.andExpect(jsonPath("$.notice.title").value("Water cut moved"));
	}

	@Test
	void officialAttachesAFileThatAnyoneCanOpen() throws Exception {
		Long id = publish();

		Long fileId = idOf(upload(id, OFFICIAL, "map.png", "image/png", PNG).andExpect(status().isCreated()));

		mvc.perform(get("/api/notices/{id}", id))
				.andExpect(jsonPath("$.files[0].id").value(fileId))
				.andExpect(jsonPath("$.files[0].fileName").value("map.png"))
				.andExpect(jsonPath("$.files[0].contentType").value("image/png"));
		mvc.perform(get("/api/notices/files/{fileId}", fileId))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/png"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("map.png")))
				.andExpect(content().bytes(PNG));
	}

	@Test
	void disguisedOrForeignUploadsAreRefused() throws Exception {
		Long id = publish();

		upload(id, OFFICIAL, "photo.png", "image/png", EXE)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("FILE_NOT_ALLOWED"))
				.andExpect(jsonPath("$.reason").value("CONTENT_MISMATCH"));
		upload(id, RESIDENT, "map.png", "image/png", PNG)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("NOT_ALLOWED"));
		upload(404L, OFFICIAL, "map.png", "image/png", PNG)
				.andExpect(status().isNotFound());
	}

	@Test
	void withdrawnNoticeAndItsFilesDisappear() throws Exception {
		Long id = publish();
		Long fileId = idOf(upload(id, OFFICIAL, "map.png", "image/png", PNG));

		mvc.perform(post("/api/notices/{id}/withdraw", id).headers(OFFICIAL))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/notices")).andExpect(jsonPath("$").isEmpty());
		mvc.perform(get("/api/notices/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
		mvc.perform(get("/api/notices/files/{fileId}", fileId)).andExpect(status().isNotFound());
		mvc.perform(put("/api/notices/{id}", id).headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("Too late")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("NOTICE_NOT_ACTIVE"));
	}

	@Test
	void complaintPhotosAreNeverServedByTheNoticeRoutes() throws Exception {
		fileStore.write("complaint-photo-1", JPEG);
		Long photoId = attachments.save(Attachment.create("pothole.jpg", "image/jpeg", JPEG.length,
				AttachmentOwner.ofComplaint(42L), "complaint-photo-1", Instant.now())).getId();

		mvc.perform(get("/api/notices/files/{fileId}", photoId).headers(OFFICIAL))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
	}

	@Test
	void complaintPhotosStayLockedUntilComplaintsProvidesItsCheck() {
		fileStore.write("complaint-photo-2", JPEG);
		Long photoId = attachments.save(Attachment.create("pothole.jpg", "image/jpeg", JPEG.length,
				AttachmentOwner.ofComplaint(42L), "complaint-photo-2", Instant.now())).getId();
		MockHttpServletRequest official = new MockHttpServletRequest();
		OFFICIAL.forEach((name, values) -> values.forEach(value -> official.addHeader(name, value)));
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(official));
		try {
			assertThat(complaintAccess.canView(3L, 42L)).isFalse();
			assertThatThrownBy(() -> openAttachment.openComplaintPhoto(42L, photoId))
					.isInstanceOf(NotAllowedException.class);
			assertThatThrownBy(() -> storeAttachment.store(AttachmentOwner.ofComplaint(42L),
					new FileUpload("pothole.jpg", "image/jpeg", JPEG)))
					.isInstanceOf(NotAllowedException.class);
			assertThat(attachments.findByOwner(AttachmentOwner.ofComplaint(42L))).hasSize(1);
		} finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}

	private Long publish() throws Exception {
		return idOf(mvc.perform(post("/api/notices").headers(OFFICIAL).contentType(MediaType.APPLICATION_JSON)
				.content(noticeJson("Water cut")))
				.andExpect(status().isCreated()));
	}

	private ResultActions upload(Long noticeId, HttpHeaders user, String name, String type, byte[] bytes)
			throws Exception {
		return mvc.perform(multipart("/api/notices/{id}/files", noticeId)
				.file(new MockMultipartFile("file", name, type, bytes))
				.headers(user));
	}

	private static Long idOf(ResultActions result) throws Exception {
		String body = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	private static String noticeJson(String title) {
		return noticeJson(title, "No water from 08:00 to 14:00.");
	}

	private static String noticeJson(String title, String content) {
		return """
				{"title": "%s", "content": "%s", "category": "WATER",
				 "neighbourhoodIds": [], "urgent": false, "expiresOn": "%s"}
				""".formatted(title, content, NEXT_WEEK);
	}

	private static HttpHeaders user(long id, String role) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Stub-User-Id", String.valueOf(id));
		headers.add("X-Stub-Role", role);
		return headers;
	}
}
