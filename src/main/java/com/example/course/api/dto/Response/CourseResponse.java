package com.example.course.api.dto.Response;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "Course representation")
public class CourseResponse {

    @Schema(description = "Identifier of the course", example = "\"1\"")
    private final String courseId;

    @Schema(description = "Course title", example = "주말 데이트 코스")
    private final String title;

    @Schema(description = "Course description", example = "서울숲 산책과 카페 방문 코스")
    private final String description;

    @Schema(description = "Course score", example = "10")
    private final Long score;

    @Schema(description = "Ordered list of POIs that compose the course")
    private final List<PoiSetResponse> poiList;

    private CourseResponse(String courseId,
                           String title,
                           String description,
                           Long score,
                           List<PoiSetResponse> poiList) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
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
                course.getId() != null ? String.valueOf(course.getId()) : null,
                course.getTitle(),
                course.getDescription(),
                course.getScore(),
                poiList
        );
    }

    public String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getScore() {
        return score;
    }

    public List<PoiSetResponse> getPoiList() {
        return poiList;
    }

    public static class PoiSetResponse {

        @Schema(description = "Identifier of the course-to-poi association", example = "11")
        private final Long poiSetId;

        @Schema(description = "Order of the POI within the course", example = "1")
        private final Integer order;

        @Schema(description = "Detailed POI information")
        private final PoiResponse poi;

        private PoiSetResponse(Long poiSetId, Integer order, PoiResponse poi) {
            this.poiSetId = poiSetId;
            this.order = order;
            this.poi = poi;
        }

        private static PoiSetResponse from(PoiSet poiSet) {
            Poi poi = poiSet.getPoi();
            return new PoiSetResponse(
                    poiSet.getId(),
                    poiSet.getOrderIndex(),
                    poi != null ? PoiResponse.from(poi) : null
            );
        }

        public Long getPoiSetId() {
            return poiSetId;
        }

        public Integer getOrder() {
            return order;
        }

        public PoiResponse getPoi() {
            return poi;
        }
    }

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

        @Schema(description = "Mood tag identifier in camelCase", example = "warmVibes")
        private final String moodTag;

        @Schema(description = "Food tags", example = "[\"coffee\", \"dessert\"]")
        private final List<String> foodTag;

        @Schema(description = "External link", example = "https://example.com")
        private final String link;

        @Schema(description = "Average rating", example = "4.3")
        private final Double ratingAvg;

        private PoiResponse(Long poiId,
                            String name,
                            String category,
                            Double lat,
                            Double lng,
                            Boolean indoor,
                            Integer priceLevel,
                            Map<String, String> openHours,
                            Integer alcohol,
                            String moodTag,
                            List<String> foodTag,
                            String link,
                            Double ratingAvg) {
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
            this.link = link;
            this.ratingAvg = ratingAvg;
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
                    poi.getLink(),
                    poi.getRatingAvg()
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

        public String getMoodTag() {
            return moodTag;
        }

        public List<String> getFoodTag() {
            return foodTag;
        }

        public String getLink() {
            return link;
        }

        public Double getRatingAvg() {
            return ratingAvg;
        }
    }
}
