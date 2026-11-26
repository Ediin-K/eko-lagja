package com.ekolagja.eko_lagja.notices.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ekolagja.eko_lagja.notices.application.port.in.CreateNotificationUseCase;
import com.ekolagja.eko_lagja.notices.domain.NotificationType;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class NotificationControllerTest {

	private static final HttpHeaders RESIDENT = user(21);
	private static final HttpHeaders NEIGHBOUR = user(22);

	@Autowired
	private MockMvc mvc;

	@Autowired
	private CreateNotificationUseCase createNotification;

	@Test
	void theBellNeedsALoggedInUser() throws Exception {
		mvc.perform(get("/api/notifications"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("NOT_ALLOWED"));
	}

	@Test
	void residentSeesOwnNotificationsAndMarksThemRead() throws Exception {
		createNotification.create(NotificationType.NEW_NOTICE, Set.of(21L, 22L), 4L);

		String body = mvc.perform(get("/api/notifications").headers(RESIDENT))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].type").value("NEW_NOTICE"))
				.andExpect(jsonPath("$.items[0].itemId").value(4))
				.andExpect(jsonPath("$.items[0].read").value(false))
				.andExpect(jsonPath("$.unreadCount").value(1))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		long id = ((Number) JsonPath.read(body, "$.items[0].id")).longValue();

		mvc.perform(post("/api/notifications/{id}/read", id).headers(NEIGHBOUR))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
		mvc.perform(post("/api/notifications/{id}/read", id).headers(RESIDENT))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/notifications").headers(RESIDENT))
				.andExpect(jsonPath("$.items[0].read").value(true))
				.andExpect(jsonPath("$.unreadCount").value(0));
	}

	private static HttpHeaders user(long id) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Stub-User-Id", String.valueOf(id));
		headers.add("X-Stub-Role", "RESIDENT");
		return headers;
	}
}
