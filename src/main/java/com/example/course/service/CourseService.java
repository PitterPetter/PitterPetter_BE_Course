package com.example.course.service;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.api.dto.Requset.CreateCourseRequest.PoiItem;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.domain.service.CourseDomainService;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.PoiRepository;
import com.example.course.repository.PoiSetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class CourseService {

    private static final String LOG_PREFIX = "[CourseService]";

    private final CourseRepository courseRepository;
    private final PoiRepository poiRepository;
    private final PoiSetRepository poiSetRepository;
    private final CourseDomainService courseDomainService;

    public CourseService(CourseRepository courseRepository,
                         PoiRepository poiRepository,
                         PoiSetRepository poiSetRepository,
                         CourseDomainService courseDomainService) {
        this.courseRepository = courseRepository;
        this.poiRepository = poiRepository;
        this.poiSetRepository = poiSetRepository;
        this.courseDomainService = courseDomainService;
    }


    public CourseCreationResult createCourse(String coupleId, CreateCourseRequest request) {
        log.info("{} 코스 생성 요청 coupleId={} title={} poiCount={}", LOG_PREFIX, coupleId, request.title(), request.data().size());

        // 도메인 검증
        courseDomainService.validateCourseCreation(coupleId, request.title(), request.data());

        // 코스 생성 및 초기화
        Course course = new Course();
        course.initialize(coupleId, request.title(), request.explain());

        Course persistedCourse = courseRepository.save(course);
        log.info("{} 코스 저장 완료 courseId={} coupleId={}", LOG_PREFIX, persistedCourse.getId(), coupleId);

        // POI 처리
        List<PoiSet> poiSets = new ArrayList<>();
        List<PoiItem> items = request.data();
        for (int i = 0; i < items.size(); i++) {
            PoiItem item = items.get(i);
            // 데이터 정규화 수행
            PoiItem normalizedItem = item.normalizeData();
            
            Poi poi = upsertPoi(normalizedItem);
            Integer order = normalizedItem.seq() != null ? normalizedItem.seq() : i + 1;

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
        Optional<Poi> existingOptional = poiRepository.findByNameAndLatAndLng(item.name(), item.lat(), item.lng());
        if (existingOptional.isPresent()) {
            Poi existing = existingOptional.get();
            log.info("{} POI 업데이트 name={} lat={} lng={} poiId={}", LOG_PREFIX, item.name(), item.lat(), item.lng(), existing.getId());
            
            // 도메인 서비스를 통한 POI 데이터 정규화
            Poi normalizedPoi = courseDomainService.normalizePoiData(item);
            existing.updateFrom(normalizedPoi);
            
            Poi updated = poiRepository.save(existing);
            log.info("{} POI 업데이트 완료 poiId={}", LOG_PREFIX, updated.getId());
            return updated;
        }

        // 도메인 서비스를 통한 POI 데이터 정규화
        Poi poi = courseDomainService.normalizePoiData(item);
        Poi saved = poiRepository.save(poi);
        log.info("{} 신규 POI 저장 name={} poiId={}", LOG_PREFIX, item.name(), saved.getId());
        return saved;
    }


    public record CourseCreationResult(Course course, List<PoiSet> poiSets) {
    }

}
