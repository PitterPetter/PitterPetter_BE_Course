package com.example.course.api;

import com.example.course.api.controller.CourseController;
import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.api.dto.Response.StatusResponse;
import com.example.course.domain.Category;
import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.jwt.JwtProvider;
import com.example.course.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "jwt.secret=test-secret-test-secret-test-secret-test")
@MockBean(JpaMetamodelMappingContext.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("POST /api/courses returns success status")
    void createCourseSuccess() throws Exception {
        Mockito.when(jwtProvider.extractCoupleId(any(Jwt.class))).thenReturn(5678L);
        Mockito.when(courseService.createCourse(anyLong(), any(CreateCourseRequest.class)))
                .thenReturn(new CourseService.CourseCreationResult(new Course(), List.of()));

        withAuthentication(5678L, null, () -> mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success")));
    }

    @Test
    @DisplayName("GET /api/courses returns all courses for couple with poi list")
    void getCoursesSuccess() throws Exception {
        Mockito.when(jwtProvider.extractCoupleId(any(Jwt.class))).thenReturn(5678L);
        Mockito.when(courseService.findCoursesByCoupleId(5678L))
                .thenReturn(List.of(
                        course(1L, 5678L, true),
                        course(2L, 5678L, true)
                ));

        withAuthentication(5678L, null, () -> mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].course_id").value(1L))
                .andExpect(jsonPath("$[0].poi_list[0].poi.poi_id").value(101L))
                .andExpect(jsonPath("$[1].course_id").value(2L)));
    }

    @Test
    @DisplayName("GET /api/courses returns 404 when no courses for couple")
    void getCoursesNotFound() throws Exception {
        Mockito.when(jwtProvider.extractCoupleId(any(Jwt.class))).thenReturn(5678L);
        Mockito.when(courseService.findCoursesByCoupleId(5678L))
                .thenThrow(new EntityNotFoundException("Courses not found for coupleId: 5678"));

        withAuthentication(5678L, null, () -> mockMvc.perform(get("/api/courses"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error")));
    }

    @Test
    @DisplayName("DELETE /api/courses deletes course using JWT courseId")
    void deleteCourseSuccess() throws Exception {
        Mockito.when(jwtProvider.extractCoupleId(any(Jwt.class))).thenReturn(5678L);
        Mockito.when(jwtProvider.getCourseIdFromJwt(any(Jwt.class))).thenReturn(3L);
        Mockito.doNothing().when(courseService).deleteCourse(5678L, 3L);

        withAuthentication(5678L, 3L, () -> mockMvc.perform(delete("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success")));
    }

    @Test
    @DisplayName("DELETE /api/courses returns 404 when course missing")
    void deleteCourseNotFound() throws Exception {
        Mockito.when(jwtProvider.extractCoupleId(any(Jwt.class))).thenReturn(5678L);
        Mockito.when(jwtProvider.getCourseIdFromJwt(any(Jwt.class))).thenReturn(3L);
        Mockito.doThrow(new EntityNotFoundException("Course not found"))
                .when(courseService).deleteCourse(5678L, 3L);

        withAuthentication(5678L, 3L, () -> mockMvc.perform(delete("/api/courses"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error")));
    }

    @Test
    @DisplayName("Missing Authentication returns 400")
    void missingAuthentication() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    private void withAuthentication(Long coupleId, Long courseId, RunnableWithException invocation) throws Exception {
        TestingAuthenticationToken authentication = jwtAuthentication(coupleId, courseId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            invocation.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private TestingAuthenticationToken jwtAuthentication(Long coupleId, Long courseId) {
        Jwt.Builder builder = Jwt.withTokenValue("token-" + coupleId)
                .header("alg", "HS256")
                .claim("coupleId", coupleId);
        if (courseId != null) {
            builder.claim("courseId", courseId);
        }
        Jwt jwt = builder.build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(jwt, null);
        authentication.setAuthenticated(true);
        return authentication;
    }

    private String validPayload() {
        return """
                {
                  \"title\": \"한강 저녁 데이터 \",
                  \"explain\": \"오늘 무드에 맞는 코스입니다~ \",
                  \"data\": [
                    {
                      \"seq\": 1,
                      \"name\": \"Blue Bottle Yeonnam\",
                      \"category\": \"CAFE\",
                      \"lat\": 37.56231,
                      \"lng\": 126.92501,
                      \"indoor\": true,
                      \"price_level\": 2,
                      \"open_hours\": {
                        \"mon\": \"09:00-18:00\"
                      },
                      \"alcohol\": 0,
                      \"mood_tag\": 1001,
                      \"food_tag\": [\"COFFEE\", \"DESSERT\"],
                      \"rating_avg\": 4.3,
                      \"link\": \"https://example.com\"
                    }
                  ]
                }
                """;
    }

    private Course course(Long id, Long coupleId, boolean includePoi) {
        Course course = new Course();
        course.setCoupleId(coupleId);
        course.setTitle("주말 데이트 코스");
        course.setInfo("서울숲 산책과 카페 방문");
        course.setScore(10L);
        setField(course, "id", id);
        setField(course, "createdAt", Instant.now());

        if (includePoi) {
            Poi poi = new Poi();
            setField(poi, "id", 101L);
            poi.setName("Blue Bottle Yeonnam");
            poi.setCategory(Category.CAFE);
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
        }

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

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }
}
