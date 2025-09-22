package com.example.poi.api.dto;

import com.example.poi.domain.Course;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourseResponse {

    private Long id;
    private String name;
    private String category;
    private Double lat;
    private Double lng;
    private Boolean indoor;
    private Integer priceLevel;
    private Map<String, String> openHours;
    private Integer alcohol;
    private Integer moodTag;
    private List<String> foodTag;
    private Double ratingAvg;
    private String link;
    private Instant createdAt;
    private Instant updatedAt;

    public static CourseResponse from(Course course) {
        CourseResponse response = new CourseResponse();
        response.id = course.getId();
        response.name = course.getName();
        response.category = course.getCategory().name();
        response.lat = course.getLat();
        response.lng = course.getLng();
        response.indoor = course.getIndoor();
        response.priceLevel = course.getPriceLevel();
        response.openHours = course.getOpenHours() != null
                ? new LinkedHashMap<>(course.getOpenHours())
                : new LinkedHashMap<>();
        response.alcohol = course.getAlcohol();
        response.moodTag = course.getMoodTag();
        response.foodTag = course.getFoodTag();
        response.ratingAvg = course.getRatingAvg();
        response.link = course.getLink();
        response.createdAt = course.getCreatedAt();
        response.updatedAt = course.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
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

    public Integer getMoodTag() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
