package com.example.course.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course", indexes = {
    @Index(name = "idx_course_couple_id", columnList = "couple_id"),
    @Index(name = "idx_course_couple_created", columnList = "couple_id, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 100)
    private String id;

    @Column(name = "couple_id", nullable = false, length = 100)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Long score = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoiSet> poiSets = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PoiSet> getPoiSets() {
        return poiSets;
    }

    public void addPoiSet(PoiSet poiSet) {
        poiSets.add(poiSet);
        poiSet.setCourse(this);
    }

    // 비즈니스 로직 메서드들

    /**
     * 코스 점수 업데이트
     */
    public void updateScore(Long score) {
        if (score == null) {
            throw new IllegalArgumentException("Score cannot be null");
        }
        if (score < 0 || score > 10) {
            throw new IllegalArgumentException("Score must be between 0 and 10");
        }
        this.score = score;
    }

    /**
     * 코스 제목 업데이트
     */
    public void updateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title cannot exceed 200 characters");
        }
        this.title = title.trim();
    }

    /**
     * 코스 설명 업데이트
     */
    public void updateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (description.length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        this.description = description.trim();
    }

    /**
     * 코스가 비어있는지 확인
     */
    public boolean isEmpty() {
        return poiSets == null || poiSets.isEmpty();
    }

    /**
     * POI 개수 반환
     */
    public int getPoiCount() {
        return poiSets == null ? 0 : poiSets.size();
    }

    /**
     * 코스 생성 시 초기화
     */
    public void initialize(String coupleId, String title, String description) {
        if (coupleId == null || coupleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Couple ID cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        
        this.coupleId = coupleId.trim();
        this.title = title.trim();
        this.description = description.trim();
        this.score = 0L;
    }
}
