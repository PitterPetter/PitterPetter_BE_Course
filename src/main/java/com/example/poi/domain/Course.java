package com.example.poi.domain;

import com.example.poi.domain.converter.OpenHoursConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "course")
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Category category;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(nullable = false)
    private Boolean indoor;

    @Column(name = "price_level")
    private Integer priceLevel;

    @Convert(converter = OpenHoursConverter.class)
    @Column(name = "open_hours", columnDefinition = "TEXT")
    private Map<String, String> openHours = new LinkedHashMap<>();

    private Integer alcohol;

    @Column(name = "mood_tag", nullable = false)
    private Integer moodTag;

    @ElementCollection
    @CollectionTable(name = "course_food_tags", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "food_tag", length = 30)
    @OrderColumn(name = "display_order")
    private List<String> foodTag = new ArrayList<>();

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(length = 2048)
    private String link;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setIndoor(Boolean indoor) {
        this.indoor = indoor;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public void setOpenHours(Map<String, String> openHours) {
        this.openHours = openHours != null ? new LinkedHashMap<>(openHours) : new LinkedHashMap<>();
    }

    public void setAlcohol(Integer alcohol) {
        this.alcohol = alcohol;
    }

    public void setMoodTag(Integer moodTag) {
        this.moodTag = moodTag;
    }

    public void setFoodTag(List<String> foodTag) {
        this.foodTag = foodTag != null ? new ArrayList<>(foodTag) : new ArrayList<>();
    }

    public void setRatingAvg(Double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
