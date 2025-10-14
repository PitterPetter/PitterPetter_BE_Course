package com.example.course.api.dto.Response;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;

import java.util.List;
import java.util.stream.Collectors;

public class CourseGetResponse {

    private final String courseId;
    private final String title;
    private final String description;
    private final Long score;
    private final List<PoiSetResponse> poiSets;

    private CourseGetResponse(String courseId,
                              String title,
                              String description,
                              Long score,
                              List<PoiSetResponse> poiSets) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.score = score;
        this.poiSets = poiSets;
    }

    public static CourseGetResponse from(Course course, List<PoiSet> poiSets) {
        List<PoiSetResponse> responses = poiSets.stream()
                .map(PoiSetResponse::from)
                .collect(Collectors.toList());
        return new CourseGetResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getScore(),
                responses
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

    public List<PoiSetResponse> getPoiSets() {
        return poiSets;
    }

    public static class PoiSetResponse {

        private final Long poiSetId;
        private final Integer order;
        private final Long poiId;
        private final String name;
        private final String category;
        private final Double lat;
        private final Double lng;

        private PoiSetResponse(Long poiSetId,
                               Integer order,
                               Long poiId,
                               String name,
                               String category,
                               Double lat,
                               Double lng) {
            this.poiSetId = poiSetId;
            this.order = order;
            this.poiId = poiId;
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lng = lng;
        }

        private static PoiSetResponse from(PoiSet poiSet) {
            Poi poi = poiSet.getPoi();
            return new PoiSetResponse(
                    poiSet.getId(),
                    poiSet.getOrderIndex(),
                    poi.getId(),
                    poi.getName(),
                    poi.getCategory().name(),
                    poi.getLat(),
                    poi.getLng()
            );
        }

        public Long getPoiSetId() {
            return poiSetId;
        }

        public Integer getOrder() {
            return order;
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
    }
}
