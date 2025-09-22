package com.example.poi.service;

import com.example.poi.api.dto.CreateCourseRequest;
import com.example.poi.domain.Course;
import com.example.poi.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class CourseService {

    private static final Set<String> VALID_DAYS = Set.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course create(CreateCourseRequest request) {
        Course course = mapToEntity(request);
        return courseRepository.save(course);
    }

    private Course mapToEntity(CreateCourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setCategory(request.getCategory());
        course.setLat(request.getLat());
        course.setLng(request.getLng());
        course.setIndoor(request.getIndoor());
        course.setPriceLevel(request.getPriceLevel());
        course.setOpenHours(sanitizedOpenHours(request.getOpenHours()));
        course.setAlcohol(request.getAlcohol());
        course.setMoodTag(request.getMoodTag());
        course.setFoodTag(request.getFoodTag());
        course.setRatingAvg(request.getRatingAvg());
        course.setLink(request.getLink());
        return course;
    }

    private Map<String, String> sanitizedOpenHours(Map<String, String> openHours) {
        if (openHours == null || openHours.isEmpty()) {
            return Map.of();
        }

        Map<String, String> sanitized = new LinkedHashMap<>();
        openHours.forEach((day, rawRange) -> {
            if (day == null || rawRange == null) {
                return;
            }
            String normalizedDay = day.trim().toLowerCase();
            if (!VALID_DAYS.contains(normalizedDay)) {
                return;
            }

            String[] times = rawRange.split("-");
            if (times.length != 2) {
                return;
            }
            try {
                LocalTime open = LocalTime.parse(times[0].trim(), TIME_FORMATTER);
                LocalTime close = LocalTime.parse(times[1].trim(), TIME_FORMATTER);
                if (open.isBefore(close)) {
                    sanitized.put(normalizedDay, TIME_FORMATTER.format(open) + "-" + TIME_FORMATTER.format(close));
                }
            } catch (DateTimeParseException ignored) {
                // ignore invalid entries to keep the API lenient
            }
        });
        return sanitized;
    }
}
