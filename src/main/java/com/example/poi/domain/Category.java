package com.example.poi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Category {
    ONLINE,
    OFFLINE,
    HYBRID,
    WORKSHOP,
    LECTURE,
    SEMINAR,
    BOOTCAMP,
    TUTORIAL,
    LAB,
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
