package com.example.course.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Category {
    CAFE,
    PARK,
    RESTAURANT,
    MUSEUM,
    BAR,
    SHOP,
    HOTEL,
    LIBRARY,
    GALLERY,
    OTHER;

    @JsonCreator
    public static Category from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(OTHER); // 존재하지 않는 카테고리는 OTHER로 매핑
    }

    @JsonValue
    public String value() {
        return name();
    }
}
