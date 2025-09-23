package com.example.course.api.dto.Requset;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request payload for creating a new course")
public class CreateCourseRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Title of the course", example = "주말 데이트 코스")
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Description of the course", example = "서울숲 산책과 카페 방문 코스")
    private String info;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Score for the course", example = "10")
    private Long score;

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    public Long getScore() {
        return score;
    }
}
