package com.example.course.api.dto.Requset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Payload for updating a course review score")
public class UpdateCourseReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Course review score between 1 and 5", example = "5")
    private Integer reviewScore;

    public Integer getReviewScore() {
        return reviewScore;
    }
}
