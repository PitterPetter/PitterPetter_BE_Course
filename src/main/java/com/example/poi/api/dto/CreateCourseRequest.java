package com.example.poi.api.dto;

import com.example.poi.domain.Category;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateCourseRequest {

    @NotBlank
    @Size(min = 1, max = 200)
    private String name;

    @NotNull
    private Category category;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double lat;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double lng;

    @NotNull
    private Boolean indoor;

    @Min(0)
    @Max(4)
    private Integer priceLevel;

    private Map<String, String> openHours;

    @Min(0)
    @Max(2)
    private Integer alcohol;

    @NotNull
    @Positive
    private Integer moodTag;

    @Size(max = 20)
    private List<@Size(min = 1, max = 30) String> foodTag;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double ratingAvg;

    @Size(max = 2048)
    private String link;

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
}
