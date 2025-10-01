package com.example.course.api.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated review summary for a POI")
public class ReviewSummaryResponse {

    @Schema(description = "Associated POI identifier", example = "3001")
    private final Long poiId;

    @Schema(description = "Average rating score", example = "4.5")
    private final double avgRating;

    @Schema(description = "Total number of reviews", example = "12")
    private final long reviewCount;

    @Schema(description = "Current user's rating if available", example = "5")
    private final Integer myRating;

    private ReviewSummaryResponse(Long poiId, double avgRating, long reviewCount, Integer myRating) {
        this.poiId = poiId;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
        this.myRating = myRating;
    }

    public static ReviewSummaryResponse of(Long poiId, double avgRating, long reviewCount, Integer myRating) {
        return new ReviewSummaryResponse(poiId, avgRating, reviewCount, myRating);
    }

    public Long getPoiId() {
        return poiId;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public Integer getMyRating() {
        return myRating;
    }
}
