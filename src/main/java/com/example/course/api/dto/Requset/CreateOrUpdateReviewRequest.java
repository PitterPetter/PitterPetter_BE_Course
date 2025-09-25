package com.example.course.api.dto.Requset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request payload for creating or updating a POI review")
public class CreateOrUpdateReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Rating score between 1 and 5", example = "5")
    private Integer rating;

    public Integer getRating() {
        return rating;
    }
}
