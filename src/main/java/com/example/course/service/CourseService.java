package com.example.course.service;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(Long coupleId, CreateCourseRequest request) {
        Course course = new Course();
        course.setCoupleId(coupleId);
        course.setTitle(request.getTitle());
        course.setInfo(request.getInfo());
        course.setScore(request.getScore());
        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> findCoursesByCoupleId(Long coupleId) {
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
}
