package com.example.course.service;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.api.dto.Requset.CreateCourseRequest.PoiItem;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.PoiRepository;
import com.example.course.repository.PoiSetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional
public class CourseService {

    private static final Set<String> VALID_DAYS = Set.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CourseRepository courseRepository;
    private final PoiRepository poiRepository;
    private final PoiSetRepository poiSetRepository;

    public CourseService(CourseRepository courseRepository,
                         PoiRepository poiRepository,
                         PoiSetRepository poiSetRepository) {
        this.courseRepository = courseRepository;
        this.poiRepository = poiRepository;
        this.poiSetRepository = poiSetRepository;
    }

    public CourseCreationResult createCourse(String coupleId, CreateCourseRequest request) {
        Course course = new Course();
        course.setCoupleId(coupleId);
        course.setTitle(request.getTitle());
        course.setDescription(request.getExplain());
        course.setScore(0L);

        Course persistedCourse = courseRepository.save(course);

        List<PoiSet> poiSets = new ArrayList<>();
        List<PoiItem> items = request.getData();
        for (int i = 0; i < items.size(); i++) {
            PoiItem item = items.get(i);
            Poi poi = upsertPoi(item);
            Integer order = item.getSeq() != null ? item.getSeq() : i + 1;

            PoiSet poiSet = new PoiSet();
            poiSet.setCourse(persistedCourse);
            poiSet.setPoi(poi);
            poiSet.setOrderIndex(order);
            PoiSet savedPoiSet = poiSetRepository.save(poiSet);
            poiSets.add(savedPoiSet);
        }

        persistedCourse.getPoiSets().addAll(poiSets);
        return new CourseCreationResult(persistedCourse, poiSets);
    }

    @Transactional(readOnly = true)
    public List<Course> findCoursesByCoupleId(String coupleId) {
        List<Course> courses = courseRepository.findAllByCoupleIdWithPoiSets(coupleId);
        if (courses.isEmpty()) {
            throw new EntityNotFoundException("Courses not found for coupleId: " + coupleId);
        }
        Comparator<PoiSet> orderComparator = Comparator
                .comparing(PoiSet::getOrderIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PoiSet::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        courses.forEach(course -> {
            course.getPoiSets().sort(orderComparator);
            course.getPoiSets().forEach(poiSet -> {
                Poi poi = poiSet.getPoi();
                if (poi != null && poi.getFoodTag() != null) {
                    poi.getFoodTag().size();
                }
            });
        });
        return courses;
    }

    public void deleteCourse(String coupleId, String courseId) {
        long parsedCourseId = parseCourseId(courseId);
        Course course = courseRepository.findByIdAndCoupleId(parsedCourseId, coupleId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found for coupleId: " + coupleId + ", courseId: " + courseId));
        courseRepository.delete(course);
    }

    public void updateReviewScore(String userId, String coupleId, String courseId, int reviewScore) {
        long parsedCourseId = parseCourseId(courseId);
        Course course = courseRepository.findByIdAndCoupleId(parsedCourseId, coupleId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found for coupleId: " + coupleId + ", courseId: " + courseId));
        course.setScore((long) reviewScore);
    }

    private Poi upsertPoi(PoiItem item) {
        Optional<Poi> existingOptional = poiRepository.findByNameAndLatAndLng(item.getName(), item.getLat(), item.getLng());
        if (existingOptional.isPresent()) {
            Poi existing = existingOptional.get();
            applyMandatoryPoiFields(existing, item);
            applyOptionalPoiFields(existing, item);
            return poiRepository.save(existing);
        }

        Poi poi = new Poi();
        applyMandatoryPoiFields(poi, item);
        applyOptionalPoiFields(poi, item);
        return poiRepository.save(poi);
    }

    private void applyMandatoryPoiFields(Poi poi, PoiItem item) {
        poi.setName(item.getName());
        poi.setCategory(item.getCategory());
        poi.setLat(item.getLat());
        poi.setLng(item.getLng());
        poi.setIndoor(item.getIndoor());
        String moodTag = StringUtils.trimWhitespace(item.getMoodTag());
        poi.setMoodTag(moodTag);
    }

    private void applyOptionalPoiFields(Poi poi, PoiItem item) {
        if (item.getPriceLevel() != null) {
            poi.setPriceLevel(item.getPriceLevel());
        }
        if (item.getAlcohol() != null) {
            poi.setAlcohol(item.getAlcohol());
        }
        if (item.getRatingAvg() != null) {
            poi.setRatingAvg(item.getRatingAvg());
        }
        if (item.getOpenHours() != null) {
            poi.setOpenHours(sanitizedOpenHours(item.getOpenHours()));
        }
        if (item.getFoodTag() != null) {
            List<String> sanitizedTags = item.getFoodTag().stream()
                    .filter(StringUtils::hasText)
                    .map(StringUtils::trimWhitespace)
                    .toList();
            poi.setFoodTag(sanitizedTags);
        }
        if (item.getLink() != null) {
            String normalizedLink = normalizeLink(item.getLink());
            poi.setLink(normalizedLink);
        }
    }

    private String normalizeLink(String link) {
        String trimmed = StringUtils.trimWhitespace(link);
        return StringUtils.hasText(trimmed) ? trimmed : null;
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
            String trimmedValue = rawRange.trim();
            if (trimmedValue.equalsIgnoreCase("Closed")) {
                sanitized.put(normalizedDay, "Closed");
            } else {
                sanitized.put(normalizedDay, normalizeTimeRange(trimmedValue));
            }
        });
        return sanitized;
    }

    private String normalizeTimeRange(String value) {
        String[] times = value.split("-");
        if (times.length != 2) {
            return value;
        }
        String start = times[0].trim();
        String end = times[1].trim();
        try {
            LocalTime open = LocalTime.parse(start, TIME_FORMATTER);
            start = TIME_FORMATTER.format(open);
        } catch (DateTimeParseException ignored) {
            // leave as-is
        }
        if (end.equals("24:00")) {
            return start + "-24:00";
        }
        try {
            LocalTime close = LocalTime.parse(end, TIME_FORMATTER);
            end = TIME_FORMATTER.format(close);
        } catch (DateTimeParseException ignored) {
            // leave as-is
        }
        return start + '-' + end;
    }

    public record CourseCreationResult(Course course, List<PoiSet> poiSets) {
    }

    private long parseCourseId(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            throw new EntityNotFoundException("Course not found for courseId: " + courseId);
        }
        try {
            long parsed = Long.parseLong(courseId);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new EntityNotFoundException("Course not found for courseId: " + courseId);
        }
    }
}
