package com.ekolagja.eko_lagja.notices.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "notices")
class NoticeJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false, length = 300)
	private String title;

	@Column(name = "content", nullable = false, length = 8000)
	private String content;

	@Column(name = "category", length = 50)
	private String category;

	@ElementCollection
	@CollectionTable(name = "notice_neighbourhoods", joinColumns = @JoinColumn(name = "notice_id"))
	@Column(name = "neighbourhood_id", nullable = false)
	private Set<Long> neighbourhoodIds = new HashSet<>();

	@Column(name = "urgent", nullable = false)
	private boolean urgent;

	@Column(name = "published_by", nullable = false)
	private Long publishedBy;

	@Column(name = "published_at", nullable = false)
	private Instant publishedAt;

	@Column(name = "expires_on", nullable = false)
	private LocalDate expiresOn;

	@Column(name = "withdrawn_at")
	private Instant withdrawnAt;

	protected NoticeJpaEntity() {
	}

	NoticeJpaEntity(Long publishedBy, Instant publishedAt) {
		this.publishedBy = publishedBy;
		this.publishedAt = publishedAt;
	}

	void update(String title, String content, String category, Set<Long> neighbourhoodIds,
			boolean urgent, LocalDate expiresOn, Instant withdrawnAt) {
		this.title = title;
		this.content = content;
		this.category = category;
		this.neighbourhoodIds.clear();
		this.neighbourhoodIds.addAll(neighbourhoodIds);
		this.urgent = urgent;
		this.expiresOn = expiresOn;
		this.withdrawnAt = withdrawnAt;
	}

	Long getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	String getContent() {
		return content;
	}

	String getCategory() {
		return category;
	}

	Set<Long> getNeighbourhoodIds() {
		return neighbourhoodIds;
	}

	boolean isUrgent() {
		return urgent;
	}

	Long getPublishedBy() {
		return publishedBy;
	}

	Instant getPublishedAt() {
		return publishedAt;
	}

	LocalDate getExpiresOn() {
		return expiresOn;
	}

	Instant getWithdrawnAt() {
		return withdrawnAt;
	}
}
