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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional
@Slf4j
public class CourseService {

    private static final Set<String> VALID_DAYS = Set.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String LOG_PREFIX = "[CourseService]";

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
        log.info("{} 코스 생성 요청 coupleId={} title={} poiCount={}", LOG_PREFIX, coupleId, request.getTitle(), request.getData().size());

        Course course = new Course();
        course.setCoupleId(coupleId);
        course.setTitle(request.getTitle());
        course.setDescription(request.getExplain());
        course.setScore(0L);

        Course persistedCourse = courseRepository.save(course);
        log.info("{} 코스 저장 완료 courseId={} coupleId={}", LOG_PREFIX, persistedCourse.getId(), coupleId);

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
            log.info("{} 코스-POI 매핑 저장 courseId={} poiSetId={} order={} poiId={}",
                    LOG_PREFIX, persistedCourse.getId(), savedPoiSet.getId(), order, poi.getId());
        }

        persistedCourse.getPoiSets().addAll(poiSets);
        return new CourseCreationResult(persistedCourse, poiSets);
    }

    @Transactional(readOnly = true)
    public List<Course> findCoursesByCoupleId(String coupleId) {
        log.info("{} 커플 코스 조회 coupleId={}", LOG_PREFIX, coupleId);

        List<Course> courses = courseRepository.findAllByCoupleIdWithPoiSets(coupleId);
        if (courses.isEmpty()) {
            log.info("{} 커플 코스 없음 - 빈 리스트 반환 coupleId={}", LOG_PREFIX, coupleId);
            return new ArrayList<>();
        }
        
        log.info("{} 커플 코스 조회 완료 coupleId={} courseCount={}", LOG_PREFIX, coupleId, courses.size());
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
          log.info("{} 코스 삭제 요청 coupleId={} courseId={}", LOG_PREFIX, coupleId, courseId);
          Course course = courseRepository.findByIdAndCoupleId(courseId, coupleId)
                  .orElseThrow(() -> {
                      log.warn("{} 삭제 대상 코스 없음 coupleId={} courseId={}", LOG_PREFIX, coupleId, courseId);
                      return new EntityNotFoundException("Course not found for coupleId: " + coupleId + ", courseId: " + courseId);
                  });
          courseRepository.delete(course);
          log.info("{} 코스 삭제 완료 coupleId={} courseId={}", LOG_PREFIX, coupleId, courseId);
      }

  
 public void updateReviewScore(String userId, String coupleId, String courseId, int reviewScore) {
          log.info("{} 코스 평점 업데이트 요청 coupleId={} courseId={} userId={} score={}", LOG_PREFIX, coupleId, courseId, userId, reviewScore);
   
        Course course = courseRepository.findByIdAndCoupleId(courseId, coupleId)
                .orElseThrow(() -> {
                    log.warn("{} 평점 업데이트 대상 코스 없음 coupleId={} courseId={}", LOG_PREFIX, coupleId, courseId);
                    return new EntityNotFoundException("Course not found for coupleId: " + coupleId + ", courseId: " + courseId);
                });
        course.setScore((long) reviewScore);
        log.info("{} 코스 평점 업데이트 완료 courseId={} score={}", LOG_PREFIX, course.getId(), reviewScore);
    }

    private Poi upsertPoi(PoiItem item) {
        Optional<Poi> existingOptional = poiRepository.findByNameAndLatAndLng(item.getName(), item.getLat(), item.getLng());
        if (existingOptional.isPresent()) {
            Poi existing = existingOptional.get();
            log.info("{} POI 업데이트 name={} lat={} lng={} poiId={}", LOG_PREFIX, item.getName(), item.getLat(), item.getLng(), existing.getId());
            applyMandatoryPoiFields(existing, item);
            applyOptionalPoiFields(existing, item);
            Poi updated = poiRepository.save(existing);
            log.info("{} POI 업데이트 완료 poiId={}", LOG_PREFIX, updated.getId());
            return updated;
        }

        Poi poi = new Poi();
        applyMandatoryPoiFields(poi, item);
        applyOptionalPoiFields(poi, item);
        Poi saved = poiRepository.save(poi);
        log.info("{} 신규 POI 저장 name={} poiId={}", LOG_PREFIX, item.getName(), saved.getId());
        return saved;
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

    public record CourseCreationResult(Course course, List<PoiSet> poiSets) {
    }

}
