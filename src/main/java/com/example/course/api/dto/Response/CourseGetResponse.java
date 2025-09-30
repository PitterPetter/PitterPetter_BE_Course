package com.example.course.api.dto.Response;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;

import java.util.List;
import java.util.stream.Collectors;

public class CourseGetResponse {

    private final Long courseId;
    private final Long coupleId;
    private final String title;
    private final String info;
    private final Long score;
    private final List<PoiSetResponse> poiSets;

    private CourseGetResponse(Long courseId,
                              Long coupleId,
                              String title,
                              String info,
                              Long score,
                              List<PoiSetResponse> poiSets) {
        this.courseId = courseId;
        this.coupleId = coupleId;
        this.title = title;
        this.info = info;
        this.score = score;
        this.poiSets = poiSets;
    }

    public static CourseGetResponse from(Course course, List<PoiSet> poiSets) {
        List<PoiSetResponse> responses = poiSets.stream()
                .map(PoiSetResponse::from)
                .collect(Collectors.toList());
        return new CourseGetResponse(
                course.getId(),
                course.getCoupleId(),
                course.getTitle(),
                course.getInfo(),
                course.getScore(),
                responses
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
