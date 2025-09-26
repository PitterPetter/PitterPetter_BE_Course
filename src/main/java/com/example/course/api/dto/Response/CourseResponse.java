package com.example.course.api.dto.Response;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Course representation")
public class CourseResponse {

    @Schema(description = "Identifier of the course", example = "1")
    private final Long courseId;

    @Schema(description = "Identifier of the couple", example = "5678")
    private final Long coupleId;

    @Schema(description = "Course title", example = "주말 데이트 코스")
    private final String title;

    @Schema(description = "Course description", example = "서울숲 산책과 카페 방문 코스")
    private final String info;

    @Schema(description = "Course score", example = "10")
    private final Long score;

    @Schema(description = "Ordered list of POIs that compose the course")
    private final List<PoiSetResponse> poiList;

    private CourseResponse(Long courseId,
                           Long coupleId,
                           String title,
                           String info,
                           Long score,
                           List<PoiSetResponse> poiList) {
        this.courseId = courseId;
        this.coupleId = coupleId;
        this.title = title;
        this.info = info;
        this.score = score;
        this.poiList = poiList;
    }

    public static CourseResponse from(Course course) {
        List<PoiSetResponse> poiList = course.getPoiSets() == null
                ? Collections.emptyList()
                : course.getPoiSets().stream()
                .map(PoiSetResponse::from)
                .collect(Collectors.toList());
        return new CourseResponse(
                course.getId(),
                course.getCoupleId(),
                course.getTitle(),
                course.getInfo(),
                course.getScore(),
                poiList
        );
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getCoupleId() {
        return coupleId;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    public Long getScore() {
        return score;
    }

    public List<PoiSetResponse> getPoiList() {
        return poiList;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PoiSetResponse {

        @Schema(description = "Identifier of the course-to-poi association", example = "11")
        private final Long poiSetId;

        @Schema(description = "Order of the POI within the course", example = "1")
        private final Integer order;

        @Schema(description = "Rating given to this POI within the course", example = "5")
        private final Integer rating;

        @Schema(description = "Detailed POI information")
        private final PoiResponse poi;

        private PoiSetResponse(Long poiSetId, Integer order, Integer rating, PoiResponse poi) {
            this.poiSetId = poiSetId;
            this.order = order;
            this.rating = rating;
            this.poi = poi;
        }

        private static PoiSetResponse from(PoiSet poiSet) {
            Poi poi = poiSet.getPoi();
            return new PoiSetResponse(
                    poiSet.getId(),
                    poiSet.getOrderIndex(),
                    poiSet.getRating(),
                    poi != null ? PoiResponse.from(poi) : null
            );
        }

        public Long getPoiSetId() {
            return poiSetId;
        }

        public Integer getOrder() {
            return order;
        }

        public Integer getRating() {
            return rating;
        }

        public PoiResponse getPoi() {
            return poi;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PoiResponse {

        @Schema(description = "Identifier of the POI", example = "101")
        private final Long poiId;

        @Schema(description = "POI name", example = "Blue Bottle Yeonnam")
        private final String name;

        @Schema(description = "POI category", example = "CAFE")
        private final String category;

        @Schema(description = "Latitude", example = "37.56231")
        private final Double lat;

        @Schema(description = "Longitude", example = "126.92501")
        private final Double lng;

        @Schema(description = "Indoor availability", example = "true")
        private final Boolean indoor;

        @Schema(description = "Price level", example = "2")
        private final Integer priceLevel;

        @Schema(description = "Opening hours by day", example = "{\"mon\":\"09:00-18:00\"}")
        private final Map<String, String> openHours;

        @Schema(description = "Alcohol availability", example = "1")
        private final Integer alcohol;

        @Schema(description = "Mood tag identifier", example = "1001")
        private final Long moodTag;

        @Schema(description = "Food tags", example = "[\"COFFEE\", \"DESSERT\"]")
        private final List<String> foodTag;

        @Schema(description = "Average rating", example = "4.3")
        private final Double ratingAvg;

        @Schema(description = "External link", example = "https://example.com")
        private final String link;

        private PoiResponse(Long poiId,
                            String name,
                            String category,
                            Double lat,
                            Double lng,
                            Boolean indoor,
                            Integer priceLevel,
                            Map<String, String> openHours,
                            Integer alcohol,
                            Long moodTag,
                            List<String> foodTag,
                            Double ratingAvg,
                            String link) {
            this.poiId = poiId;
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lng = lng;
            this.indoor = indoor;
            this.priceLevel = priceLevel;
            this.openHours = openHours;
            this.alcohol = alcohol;
            this.moodTag = moodTag;
            this.foodTag = foodTag;
            this.ratingAvg = ratingAvg;
            this.link = link;
        }

        private static PoiResponse from(Poi poi) {
            return new PoiResponse(
                    poi.getId(),
                    poi.getName(),
                    poi.getCategory() != null ? poi.getCategory().name() : null,
                    poi.getLat(),
                    poi.getLng(),
                    poi.getIndoor(),
                    poi.getPriceLevel(),
                    poi.getOpenHours(),
                    poi.getAlcohol(),
                    poi.getMoodTag(),
                    poi.getFoodTag() != null ? List.copyOf(poi.getFoodTag()) : List.of(),
                    poi.getRatingAvg(),
                    poi.getLink()
            );
        }

        public Long getPoiId() {
            return poiId;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
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
