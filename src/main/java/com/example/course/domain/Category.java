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
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + value));
    }

    @JsonValue
    public String value() {
        return name();
    }
}
