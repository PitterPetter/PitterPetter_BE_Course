package com.example.course.service;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    @DisplayName("createCourse persists a course with provided coupleId")
    void createCoursePersistsCourse() {
        CreateCourseRequest request = buildRequest();
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            setField(course, "id", 1L);
            return course;
        });

        Course course = courseService.createCourse(5678L, request);

        assertThat(course.getId()).isEqualTo(1L);
        assertThat(course.getCoupleId()).isEqualTo(5678L);
        assertThat(course.getTitle()).isEqualTo("주말 데이트 코스");

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertThat(captor.getValue().getCoupleId()).isEqualTo(5678L);
    }

    @Test
    @DisplayName("findCoursesByCoupleId returns courses with poi data when present")
    void findCoursesByCoupleId() {
        Course course = buildCourseWithPoi();
        when(courseRepository.findAllByCoupleIdWithPoiSets(5678L)).thenReturn(List.of(course));

        List<Course> courses = courseService.findCoursesByCoupleId(5678L);

        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getPoiSets()).hasSize(1);
        assertThat(courses.get(0).getPoiSets().get(0).getPoi().getName()).isEqualTo("Blue Bottle Yeonnam");
    }

    @Test
    @DisplayName("findCoursesByCoupleId throws when no courses exist")
    void findCoursesByCoupleIdNotFound() {
        when(courseRepository.findAllByCoupleIdWithPoiSets(5678L)).thenReturn(List.of());

        assertThatThrownBy(() -> courseService.findCoursesByCoupleId(5678L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("5678");
    }

    private CreateCourseRequest buildRequest() {
        CreateCourseRequest request = new CreateCourseRequest();
        setField(request, "title", "주말 데이트 코스");
        setField(request, "info", "서울숲 산책과 카페 방문");
        setField(request, "score", 10L);
        return request;
    }

    private Course buildCourseWithPoi() {
        Course course = new Course();
        setField(course, "id", 1L);
        course.setCoupleId(5678L);
        course.setTitle("주말 데이트 코스");
        course.setInfo("서울숲 산책과 카페 방문");
        course.setScore(10L);

        Poi poi = new Poi();
        setField(poi, "id", 101L);
        poi.setName("Blue Bottle Yeonnam");
        poi.setCategory(com.example.course.domain.Category.CAFE);
        poi.setLat(37.56231);
        poi.setLng(126.92501);
        poi.setIndoor(true);
        poi.setPriceLevel(2);
        poi.setOpenHours(Map.of("mon", "09:00-18:00"));
        poi.setAlcohol(0);
        poi.setMoodTag(1001L);
        poi.setFoodTag(List.of("COFFEE", "DESSERT"));
        poi.setRatingAvg(4.3);
        poi.setLink("https://example.com");

        PoiSet poiSet = new PoiSet();
        setField(poiSet, "id", 11L);
        poiSet.setCourse(course);
        poiSet.setPoi(poi);
        poiSet.setOrderIndex(1);
        poiSet.setRating(null);

        course.getPoiSets().add(poiSet);
        return course;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
