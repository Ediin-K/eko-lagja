package com.ekolagja.eko_lagja.notices.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

public class Notice {

	public static final ZoneId MUNICIPALITY_ZONE = ZoneId.of("Europe/Belgrade");

	private final Long id;
	private String title;
	private String content;
	private String category;
	private Set<Long> neighbourhoodIds;
	private boolean urgent;
	private final Long publishedBy;
	private final Instant publishedAt;
	private LocalDate expiresOn;
	private Instant withdrawnAt;

	private Notice(Long id, String title, String content, String category,
			Set<Long> neighbourhoodIds, boolean urgent, Long publishedBy,
			Instant publishedAt, LocalDate expiresOn, Instant withdrawnAt) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.category = category;
		this.neighbourhoodIds = neighbourhoodIds;
		this.urgent = urgent;
		this.publishedBy = publishedBy;
		this.publishedAt = publishedAt;
		this.expiresOn = expiresOn;
		this.withdrawnAt = withdrawnAt;
	}

	public static Notice publish(String title, String content, String category,
			Set<Long> neighbourhoodIds, boolean urgent, LocalDate expiresOn,
			Long publishedBy, Instant now) {
		Objects.requireNonNull(now, "now");
		if (publishedBy == null) {
			throw new InvalidNoticeException("publishedBy must be set");
		}
		LocalDate today = dayOf(now);
		requireText(title, "title");
		requireText(content, "content");
		Set<Long> ids = copyOf(neighbourhoodIds);
		requireValidExpiry(expiresOn, today);
		return new Notice(null, title, content, category, ids, urgent,
				publishedBy, now, expiresOn, null);
	}

	public static Notice restore(Long id, String title, String content, String category,
			Set<Long> neighbourhoodIds, boolean urgent, Long publishedBy, Instant publishedAt,
			LocalDate expiresOn, Instant withdrawnAt) {
		Objects.requireNonNull(id, "id");
		return new Notice(id, title, content, category, Set.copyOf(neighbourhoodIds), urgent,
				publishedBy, publishedAt, expiresOn, withdrawnAt);
	}

	public void edit(String title, String content, String category,
			Set<Long> neighbourhoodIds, boolean urgent, LocalDate expiresOn,
			LocalDate today) {
		requireActiveOn(today);
		requireText(title, "title");
		requireText(content, "content");
		Set<Long> ids = copyOf(neighbourhoodIds);
		requireValidExpiry(expiresOn, today);
		this.title = title;
		this.content = content;
		this.category = category;
		this.neighbourhoodIds = ids;
		this.urgent = urgent;
		this.expiresOn = expiresOn;
	}

	public void withdraw(Instant now) {
		Objects.requireNonNull(now, "now");
		requireActiveOn(dayOf(now));
		this.withdrawnAt = now;
	}

	public NoticeStatus statusOn(LocalDate today) {
		Objects.requireNonNull(today, "today");
		if (withdrawnAt != null) {
			return NoticeStatus.WITHDRAWN;
		}
		if (today.isAfter(expiresOn)) {
			return NoticeStatus.EXPIRED;
		}
		return NoticeStatus.ACTIVE;
	}

	public boolean isVisibleIn(Long neighbourhoodId, LocalDate today) {
		if (statusOn(today) != NoticeStatus.ACTIVE) {
			return false;
		}
		return isForWholeMunicipality()
				|| (neighbourhoodId != null && neighbourhoodIds.contains(neighbourhoodId));
	}

	public boolean isForWholeMunicipality() {
		return neighbourhoodIds.isEmpty();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getCategory() {
		return category;
	}

	public Set<Long> getNeighbourhoodIds() {
		return neighbourhoodIds;
	}

	public boolean isUrgent() {
		return urgent;
	}

	public Long getPublishedBy() {
		return publishedBy;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public LocalDate getExpiresOn() {
		return expiresOn;
	}

	public Instant getWithdrawnAt() {
		return withdrawnAt;
	}

	private void requireActiveOn(LocalDate today) {
		NoticeStatus status = statusOn(today);
		if (status != NoticeStatus.ACTIVE) {
			throw new NoticeNotActiveException(status);
		}
	}

	private static LocalDate dayOf(Instant instant) {
		return LocalDate.ofInstant(instant, MUNICIPALITY_ZONE);
	}

	private static void requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new InvalidNoticeException(name + " must not be blank");
		}
	}

	private static void requireValidExpiry(LocalDate expiresOn, LocalDate today) {
		if (expiresOn == null) {
			throw new InvalidNoticeException("expiresOn must be set");
		}
		if (expiresOn.isBefore(today)) {
			throw new InvalidNoticeException("expiresOn must be today or later");
		}
	}

	private static Set<Long> copyOf(Set<Long> neighbourhoodIds) {
		if (neighbourhoodIds == null || neighbourhoodIds.stream().anyMatch(Objects::isNull)) {
			throw new InvalidNoticeException("neighbourhoodIds must be a set of ids, empty for the whole municipality");
		}
		return Set.copyOf(neighbourhoodIds);
	}
}
