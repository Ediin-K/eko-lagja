package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class StubCurrentUserTest {

	private final StubCurrentUser currentUser = new StubCurrentUser();

	@AfterEach
	void forgetRequest() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void withoutARequestEveryoneIsAVisitor() {
		assertThat(currentUser.id()).isEmpty();
		assertThat(currentUser.isOfficial()).isFalse();
	}

	@Test
	void headersNameTheUserAndTheRole() {
		request("3", "official");

		assertThat(currentUser.id()).contains(3L);
		assertThat(currentUser.isOfficial()).isTrue();
	}

	@Test
	void anyOtherRoleIsNotAnOfficial() {
		request("21", "RESIDENT");

		assertThat(currentUser.id()).contains(21L);
		assertThat(currentUser.isOfficial()).isFalse();
	}

	@Test
	void roleWithoutAValidUserIsAVisitor() {
		for (String id : new String[] { null, "", "abc", "0", "-5" }) {
			request(id, "OFFICIAL");

			assertThat(currentUser.id()).as("id %s", id).isEmpty();
			assertThat(currentUser.isOfficial()).as("id %s", id).isFalse();
		}
	}

	private static void request(String userId, String role) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		if (userId != null) {
			request.addHeader(StubCurrentUser.USER_ID_HEADER, userId);
		}
		request.addHeader(StubCurrentUser.ROLE_HEADER, role);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}
}
