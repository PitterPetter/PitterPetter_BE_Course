package com.example.course.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "poi", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "lat", "lng"}))
@EntityListeners(AuditingEntityListener.class)
public class Poi {

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "open_hours", columnDefinition = "jsonb")
    private Map<String, String> openHours = new LinkedHashMap<>();

    private Integer alcohol;

    @Column(name = "mood_tag", nullable = false)
    private Long moodTag;

    @ElementCollection
    @CollectionTable(name = "poi_food_tags", joinColumns = @JoinColumn(name = "poi_id"))
    @Column(name = "food_tag", length = 30)
    @OrderColumn(name = "display_order")
    private List<String> foodTag = new ArrayList<>();

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(length = 2048)
    private String link;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Boolean getIndoor() {
        return indoor;
    }

    public void setIndoor(Boolean indoor) {
        this.indoor = indoor;
    }

    public Integer getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public Map<String, String> getOpenHours() {
        return openHours;
    }

    public void setOpenHours(Map<String, String> openHours) {
        this.openHours = openHours != null ? new LinkedHashMap<>(openHours) : new LinkedHashMap<>();
    }

    public Integer getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(Integer alcohol) {
        this.alcohol = alcohol;
    }

    public Long getMoodTag() {
        return moodTag;
    }

    public void setMoodTag(Long moodTag) {
        this.moodTag = moodTag;
    }

    public List<String> getFoodTag() {
        return foodTag;
    }

    public void setFoodTag(List<String> foodTag) {
        this.foodTag = foodTag != null ? new ArrayList<>(foodTag) : new ArrayList<>();
    }

    public Double getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(Double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
