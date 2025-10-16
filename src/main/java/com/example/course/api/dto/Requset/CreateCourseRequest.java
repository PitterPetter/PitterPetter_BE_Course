package com.example.course.api.dto.Requset;

import com.example.course.domain.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request payload for creating a new course")
public record CreateCourseRequest(
    @NotBlank
    @Size(min = 1, max = 200)
    @Schema(description = "Title of the course", example = "한강 저녁 데이터 ")
    String title,

    @NotBlank
    @Size(min = 1, max = 1000)
    @Schema(description = "Description of the course", example = "오늘 무드에 맞는 코스입니다~ ")
    String explain,

    @NotNull
    @Size(min = 1)
    @Valid
    @Schema(description = "Ordered POI list that composes the course")
    List<PoiItem> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PoiItem(
        @Schema(description = "Sequence order of the POI in the course", example = "1")
        Integer seq,

        @NotBlank
        @Size(min = 1, max = 200)
        @Schema(description = "POI name", example = "Blue Bottle Yeonnam")
        String name,

        @NotNull
        @Schema(description = "POI category", example = "CAFE")
        Category category,

        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        @Schema(description = "Latitude", example = "37.56231")
        Double lat,

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        @Schema(description = "Longitude", example = "126.92501")
        Double lng,

        @NotNull
        @Schema(description = "Indoor availability", example = "true")
        Boolean indoor,

        @Min(0)
        @Max(5)
        @Schema(description = "Price level", example = "2")
        Integer priceLevel,

        @Schema(description = "Opening hours map", example = "{\"mon\":\"09:00-18:00\"}")
        Map<String, String> openHours,

        @Min(0)
        @Max(5)
        @Schema(description = "Alcohol availability", example = "0")
        Integer alcohol,

        @Size(max = 50)
        @Schema(description = "Mood tag identifier in camelCase", example = "warmVibes")
        String moodTag,

        @Size(max = 20)
        @Schema(description = "Food tags", example = "[\"coffee\", \"dessert\"]")
        List<@Size(min = 1, max = 30) String> foodTag,

        @DecimalMin(value = "0.0")
        @DecimalMax(value = "5.0")
        @Schema(description = "Average rating", example = "4.3")
        Double ratingAvg,

        @Pattern(regexp = "^$|https?://.+", message = "must be a valid URL")
        @Schema(description = "External link", example = "https://example.com")
        String link
    ) {
        // 데이터 정규화 로직
        public PoiItem normalizeData() {
            // moodTag가 "0"이면 null로 처리
            String normalizedMoodTag = "0".equals(this.moodTag) ? null : this.moodTag;
            
            // alcohol이 null이면 0으로 처리
            Integer normalizedAlcohol = this.alcohol != null ? this.alcohol : 0;
            
            return new PoiItem(
                this.seq,
                this.name,
                this.category,
                this.lat,
                this.lng,
                this.indoor,
                this.priceLevel,
                this.openHours,
                normalizedAlcohol,
                normalizedMoodTag,
                this.foodTag,
                this.ratingAvg,
                this.link
            );
        }
    }
}