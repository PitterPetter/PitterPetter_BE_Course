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
public class CreateCourseRequest {

    @NotBlank
    @Size(min = 1, max = 200)
    @Schema(description = "Title of the course", example = "한강 저녁 데이터 ")
    private String title;

    @NotBlank
    @Size(min = 1, max = 1000)
    @Schema(description = "Description of the course", example = "오늘 무드에 맞는 코스입니다~ ")
    private String explain;

    @NotNull
    @Size(min = 1)
    @Valid
    @Schema(description = "Ordered POI list that composes the course")
    private List<PoiItem> data;

    public String getTitle() {
        return title;
    }

    public String getExplain() {
        return explain;
    }

    public List<PoiItem> getData() {
        return data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PoiItem {

        @Schema(description = "Sequence order of the POI in the course", example = "1")
        private Integer seq;

        @NotBlank
        @Size(min = 1, max = 200)
        @Schema(description = "POI name", example = "Blue Bottle Yeonnam")
        private String name;

        @NotNull
        @Schema(description = "POI category", example = "CAFE")
        private Category category;

        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        @Schema(description = "Latitude", example = "37.56231")
        private Double lat;

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        @Schema(description = "Longitude", example = "126.92501")
        private Double lng;

        @NotNull
        @Schema(description = "Indoor availability", example = "true")
        private Boolean indoor;

        @Min(0)
        @Max(4)
        @Schema(description = "Price level", example = "2")
        private Integer priceLevel;

        @Schema(description = "Opening hours map", example = "{\"mon\":\"09:00-18:00\"}")
        private Map<String, String> openHours;

        @Min(0)
        @Max(2)
        @Schema(description = "Alcohol availability", example = "0")
        private Integer alcohol;

        @NotNull
        @Positive
        @Schema(description = "Mood tag identifier", example = "1001")
        private Long moodTag;

        @Size(max = 20)
        @Schema(description = "Food tags", example = "[\"COFFEE\", \"DESSERT\"]")
        private List<@Size(min = 1, max = 30) String> foodTag;

        @DecimalMin(value = "0.0")
        @DecimalMax(value = "5.0")
        @Schema(description = "Average rating", example = "4.3")
        private Double ratingAvg;

        @Pattern(regexp = "^$|https?://.+", message = "must be a valid URL")
        @Schema(description = "External link", example = "https://example.com")
        private String link;

        public Integer getSeq() {
            return seq;
        }

        public String getName() {
            return name;
        }

        public Category getCategory() {
            return category;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }

        public Boolean getIndoor() {
            return indoor;
        }

        public Integer getPriceLevel() {
            return priceLevel;
        }

        public Map<String, String> getOpenHours() {
            return openHours;
        }

        public Integer getAlcohol() {
            return alcohol;
        }

        public Long getMoodTag() {
            return moodTag;
        }

        public List<String> getFoodTag() {
            return foodTag;
        }

        public Double getRatingAvg() {
            return ratingAvg;
        }

        public String getLink() {
            return link;
        }
    }
}
