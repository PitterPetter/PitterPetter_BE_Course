package com.example.course.api.dto.Response;

import com.example.course.domain.PoiReview;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "POI review representation")
public class ReviewResponse {

    @Schema(description = "Review identifier", example = "911")
    private final Long reviewId;

    @Schema(description = "Associated POI identifier", example = "3001")
    private final Long poiId;

    @Schema(description = "Reviewer user identifier", example = "42")
    private final Long userId;

    @Schema(description = "Rating score between 1 and 5", example = "5")
    private final Integer rating;

    @Schema(description = "Creation timestamp", example = "2025-09-24T06:43:12Z")
    private final Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2025-09-24T06:43:12Z")
    private final Instant updatedAt;

    private ReviewResponse(Long reviewId,
                           Long poiId,
                           Long userId,
                           Integer rating,
                           Instant createdAt,
                           Instant updatedAt) {
        this.reviewId = reviewId;
        this.poiId = poiId;
        this.userId = userId;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReviewResponse from(PoiReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getPoi() != null ? review.getPoi().getId() : null,
                review.getUserId(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getPoiId() {
        return poiId;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getRating() {
        return rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
