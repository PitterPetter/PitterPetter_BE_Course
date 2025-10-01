package com.example.course.api.dto.Requset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Bulk request payload for creating or updating POI reviews")
public class UpsertPoiReviewsRequest {

    @NotNull
    @Size(min = 1)
    @Valid
    @Schema(description = "Collection of review items to upsert")
    private List<ReviewItem> data;

    public List<ReviewItem> getData() {
        return data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewItem {

        @NotNull
        @Positive
        @Schema(description = "POI identifier", example = "3001")
        private Long poiId;

        @NotNull
        @Min(1)
        @Max(5)
        @Schema(description = "Rating score between 1 and 5", example = "5")
        private Integer rating;

        public Long getPoiId() {
            return poiId;
        }

        public Integer getRating() {
            return rating;
        }
    }
}
