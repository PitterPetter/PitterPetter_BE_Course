package com.example.course.service;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.domain.Category;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.PoiRepository;
import com.example.course.repository.PoiSetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PoiRepository poiRepository;

    @Mock
    private PoiSetRepository poiSetRepository;

    @InjectMocks
    private CourseService courseService;

    private CreateCourseRequest request;
    private CreateCourseRequest.PoiItem poiItem;

    @BeforeEach
    void setUp() throws Exception {
        request = new CreateCourseRequest();
        setField(request, "title", "주말 데이트 코스");
        setField(request, "explain", "서울숲 산책과 카페 방문");

        poiItem = new CreateCourseRequest.PoiItem();
        setField(poiItem, "seq", 1);
        setField(poiItem, "name", "Blue Bottle Yeonnam");
        setField(poiItem, "category", Category.CAFE);
        setField(poiItem, "lat", 37.56231);
        setField(poiItem, "lng", 126.92501);
        setField(poiItem, "indoor", true);
        setField(poiItem, "priceLevel", 2);
        setField(poiItem, "openHours", Map.of("mon", "09:00-18:00"));
        setField(poiItem, "alcohol", 0);
        setField(poiItem, "moodTag", 1001L);
        setField(poiItem, "foodTag", List.of("COFFEE", "DESSERT"));
        setField(poiItem, "ratingAvg", 4.3);
        setField(poiItem, "link", "https://example.com");

        setField(request, "data", List.of(poiItem));

        lenient().when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            setField(course, "id", 1L);
            return course;
        });

        lenient().when(poiSetRepository.save(any(PoiSet.class))).thenAnswer(invocation -> {
            PoiSet poiSet = invocation.getArgument(0);
            setField(poiSet, "id", 10L);
            return poiSet;
        });
    }

    @Test
    @DisplayName("새로운 POI를 생성하고 코스와 연결한다")
    void createCourseInsertsNewPoi() throws Exception {
        when(poiRepository.findByNameAndLatAndLng(anyString(), anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());
        when(poiRepository.save(any(Poi.class))).thenAnswer(invocation -> {
            Poi poi = invocation.getArgument(0);
            setField(poi, "id", 5L);
            return poi;
        });

        CourseService.CourseCreationResult result = courseService.createCourse(5678L, request);

        assertThat(result.course().getCoupleId()).isEqualTo(5678L);
        assertThat(result.poiSets()).hasSize(1);
        PoiSet savedSet = result.poiSets().get(0);
        assertThat(savedSet.getOrderIndex()).isEqualTo(1);
        assertThat(savedSet.getPoi().getId()).isEqualTo(5L);

        ArgumentCaptor<Poi> poiCaptor = ArgumentCaptor.forClass(Poi.class);
        verify(poiRepository).save(poiCaptor.capture());
        Map<String, String> openHours = poiCaptor.getValue().getOpenHours();
        assertThat(openHours).containsEntry("mon", "09:00-18:00");
    }

    @Test
    @DisplayName("기존 POI를 재사용하면서 선택적 필드를 갱신한다")
    void createCourseReusesExistingPoi() throws Exception {
        Poi existing = new Poi();
        setField(existing, "id", 8L);
        existing.setName("Blue Bottle Yeonnam");
        existing.setCategory(Category.CAFE);
        existing.setLat(37.56231);
        existing.setLng(126.92501);
        existing.setIndoor(true);
        existing.setMoodTag(1001L);
        existing.setLink(null);

        when(poiRepository.findByNameAndLatAndLng("Blue Bottle Yeonnam", 37.56231, 126.92501))
                .thenReturn(Optional.of(existing));
        when(poiRepository.save(existing)).thenReturn(existing);

        CourseService.CourseCreationResult result = courseService.createCourse(5678L, request);

        assertThat(result.poiSets()).hasSize(1);
        Poi reused = result.poiSets().get(0).getPoi();
        assertThat(reused.getId()).isEqualTo(8L);
        assertThat(reused.getPriceLevel()).isEqualTo(2);
        assertThat(reused.getLink()).isEqualTo("https://example.com");
        assertThat(reused.getFoodTag()).containsExactly("COFFEE", "DESSERT");
    }

    @Test
    @DisplayName("코스를 조회하면 Course 목록을 반환한다")
    void findCoursesByCoupleIdReturnsCourses() throws Exception {
        Course course = new Course();
        setField(course, "id", 1L);
        course.setCoupleId(5678L);
        course.setTitle("주말 데이트 코스");
        course.setInfo("서울숲 산책과 카페 방문");
        course.setScore(0L);

        Poi poi = new Poi();
        setField(poi, "id", 5L);
        poi.setName("Blue Bottle Yeonnam");
        poi.setCategory(Category.CAFE);
        poi.setLat(37.56231);
        poi.setLng(126.92501);
        poi.setIndoor(true);
        poi.setMoodTag(1001L);
        poi.setFoodTag(new ArrayList<>(List.of("COFFEE")));

        PoiSet poiSet = new PoiSet();
        setField(poiSet, "id", 20L);
        poiSet.setCourse(course);
        poiSet.setPoi(poi);
        poiSet.setOrderIndex(1);

        course.getPoiSets().add(poiSet);

        when(courseRepository.findAllByCoupleIdWithPoiSets(5678L)).thenReturn(List.of(course));

        List<Course> courses = courseService.findCoursesByCoupleId(5678L);

        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getPoiSets()).hasSize(1);
        assertThat(courses.get(0).getPoiSets().get(0).getPoi().getFoodTag()).containsExactly("COFFEE");
    }

    @Test
    @DisplayName("커플 코스가 없으면 예외가 발생한다")
    void findCoursesByCoupleIdNotFound() {
        when(courseRepository.findAllByCoupleIdWithPoiSets(5678L)).thenReturn(List.of());

        assertThatThrownBy(() -> courseService.findCoursesByCoupleId(5678L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("5678");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
