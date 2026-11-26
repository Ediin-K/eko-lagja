package com.ekolagja.eko_lagja.notices.adapter.out.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ekolagja.eko_lagja.notices.application.port.out.CurrentUser;

class CurrentUserProfileTest {

	@Test
	void headerStubRunsOnlyInTheDevProfile() {
		assertThat(currentUserWithProfiles("dev")).isInstanceOf(StubCurrentUser.class);
	}

	@Test
	void everyOtherProfileHasNoLoggedInUser() {
		for (String[] profiles : new String[][] { {}, { "prod" }, { "dev", "prod" }, { "test" } }) {
			assertThat(currentUserWithProfiles(profiles)).as(String.join(",", profiles))
					.isInstanceOf(StubAnonymousCurrentUser.class);
		}
	}

	@Test
	void outsideDevTheStubHeadersAreIgnored() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(StubCurrentUser.USER_ID_HEADER, "3");
		request.addHeader(StubCurrentUser.ROLE_HEADER, "OFFICIAL");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		try {
			CurrentUser currentUser = currentUserWithProfiles("dev", "prod");

			assertThat(currentUser.id()).isEmpty();
			assertThat(currentUser.isOfficial()).isFalse();
		} finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}

	private static CurrentUser currentUserWithProfiles(String... profiles) {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.getEnvironment().setActiveProfiles(profiles);
			context.register(StubCurrentUser.class, StubAnonymousCurrentUser.class);
			context.refresh();
			return context.getBean(CurrentUser.class);
		}
	}
}
