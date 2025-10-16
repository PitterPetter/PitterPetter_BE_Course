package com.example.course.domain.service;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.api.dto.Requset.CreateCourseRequest.PoiItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
public class CourseDomainService {

    private static final Set<String> VALID_DAYS = Set.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String LOG_PREFIX = "[CourseDomainService]";

    /**
     * 코스 생성 시 도메인 검증
     */
    public void validateCourseCreation(String coupleId, String title, List<PoiItem> poiItems) {
        if (!StringUtils.hasText(coupleId)) {
            throw new IllegalArgumentException("Couple ID cannot be empty");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (poiItems == null || poiItems.isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one POI");
        }
        
        // POI 검증
        for (int i = 0; i < poiItems.size(); i++) {
            PoiItem item = poiItems.get(i);
            validatePoiItem(item, i);
        }
    }

    /**
     * POI 아이템 검증
     */
    private void validatePoiItem(PoiItem item, int index) {
        if (!StringUtils.hasText(item.name())) {
            throw new IllegalArgumentException("POI name cannot be empty at index " + index);
        }
        if (item.category() == null) {
            throw new IllegalArgumentException("POI category cannot be null at index " + index);
        }
        if (item.lat() == null || item.lat() < -90.0 || item.lat() > 90.0) {
            throw new IllegalArgumentException("Invalid latitude at index " + index);
        }
        if (item.lng() == null || item.lng() < -180.0 || item.lng() > 180.0) {
            throw new IllegalArgumentException("Invalid longitude at index " + index);
        }
        if (item.indoor() == null) {
            throw new IllegalArgumentException("Indoor flag cannot be null at index " + index);
        }
        
        // moodTag 검증은 정규화 후에 수행됨
    }

    /**
     * 코스 점수 업데이트 검증
     */
    public void validateScoreUpdate(Long score) {
        if (score == null) {
            throw new IllegalArgumentException("Score cannot be null");
        }
        if (score < 0 || score > 10) {
            throw new IllegalArgumentException("Score must be between 0 and 10");
        }
    }

    /**
     * POI 데이터 정규화
     */
    public Poi normalizePoiData(PoiItem item) {
        Poi poi = new Poi();
        
        // 필수 필드 설정
        poi.setName(item.name());
        poi.setCategory(item.category());
        poi.setLat(item.lat());
        poi.setLng(item.lng());
        poi.setIndoor(item.indoor());
        
        // 선택적 필드 설정
        if (item.priceLevel() != null) {
            poi.setPriceLevel(item.priceLevel());
        }
        if (item.alcohol() != null) {
            poi.setAlcohol(item.alcohol());
        }
        if (item.ratingAvg() != null) {
            poi.setRatingAvg(item.ratingAvg());
        }
        if (item.openHours() != null) {
            poi.setOpenHours(sanitizedOpenHours(item.openHours()));
        }
        if (item.foodTag() != null) {
            List<String> sanitizedTags = item.foodTag().stream()
                    .filter(StringUtils::hasText)
                    .map(StringUtils::trimWhitespace)
                    .toList();
            poi.setFoodTag(sanitizedTags);
        }
        if (item.link() != null) {
            String normalizedLink = normalizeLink(item.link());
            poi.setLink(normalizedLink);
        }
        
        // moodTag 설정 (null 허용)
        String moodTag = StringUtils.trimWhitespace(item.moodTag());
        poi.setMoodTag(moodTag);
        
        return poi;
    }

    /**
     * 링크 정규화
     */
    private String normalizeLink(String link) {
        String trimmed = StringUtils.trimWhitespace(link);
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    /**
     * 영업시간 정규화
     */
    private Map<String, String> sanitizedOpenHours(Map<String, String> openHours) {
        if (openHours == null || openHours.isEmpty()) {
            return Map.of();
        }
        Map<String, String> sanitized = new LinkedHashMap<>();
        openHours.forEach((day, rawRange) -> {
            if (day == null || rawRange == null) {
                log.warn("{} 영업시간 항목 무시 day={} raw={} (null)", LOG_PREFIX, day, rawRange);
                return;
            }
            String normalizedDay = day.trim().toLowerCase();
            if (!VALID_DAYS.contains(normalizedDay)) {
                log.warn("{} 유효하지 않은 요일 값 day={}", LOG_PREFIX, day);
                return;
            }
            String trimmedValue = rawRange.trim();
            if (trimmedValue.equalsIgnoreCase("Closed")) {
                sanitized.put(normalizedDay, "Closed");
            } else {
                sanitized.put(normalizedDay, normalizeTimeRange(trimmedValue));
            }
        });
        return sanitized;
    }

    /**
     * 시간 범위 정규화
     */
    private String normalizeTimeRange(String value) {
        String[] times = value.split("-");
        if (times.length != 2) {
            log.warn("{} 영업시간 포맷이 잘못됨 value={} (하이픈 미존재)", LOG_PREFIX, value);
            return value;
        }
        String start = times[0].trim();
        String end = times[1].trim();
        try {
            LocalTime open = LocalTime.parse(start, TIME_FORMATTER);
            start = TIME_FORMATTER.format(open);
        } catch (DateTimeParseException ignored) {
            log.warn("{} 시작 시간 파싱 실패 value={}", LOG_PREFIX, start);
        }
        if (end.equals("24:00")) {
            return start + "-24:00";
        }
        try {
            LocalTime close = LocalTime.parse(end, TIME_FORMATTER);
            end = TIME_FORMATTER.format(close);
        } catch (DateTimeParseException ignored) {
            log.warn("{} 종료 시간 파싱 실패 value={}", LOG_PREFIX, end);
        }
        return start + '-' + end;
    }
}
