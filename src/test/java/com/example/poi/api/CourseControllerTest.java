package com.example.poi.api;

import com.example.poi.domain.Category;
import com.example.poi.domain.Course;
import com.example.poi.exception.GlobalExceptionHandler;
import com.example.poi.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    private Course persistedCourse;

    @BeforeEach
    void setUp() {
        persistedCourse = new Course();
        persistedCourse.setId(1L);
        persistedCourse.setName("Intro to Spring");
        persistedCourse.setCategory(Category.ONLINE);
        persistedCourse.setLat(37.56231);
        persistedCourse.setLng(126.92501);
        persistedCourse.setIndoor(true);
        persistedCourse.setMoodTag(1001);
        persistedCourse.setCreatedAt(Instant.parse("2025-01-01T10:00:00Z"));
        persistedCourse.setUpdatedAt(Instant.parse("2025-01-01T10:00:00Z"));
    }

    @Test
    @DisplayName("정상 요청 시 201과 success 응답을 반환한다")
    void createCourseSuccess() throws Exception {
        Mockito.when(courseService.create(any())).thenReturn(persistedCourse);

        String payload = """
                {
                  \"name\": \"Intro to Spring\",
                  \"category\": \"ONLINE\",
                  \"lat\": 37.56231,
                  \"lng\": 126.92501,
                  \"indoor\": true,
                  \"mood_tag\": 1001
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Intro to Spring"));
    }

    @Test
    @DisplayName("필수 필드 누락 시 400을 반환한다")
    void createCourseMissingRequired() throws Exception {
        String payload = """
                {
                  \"category\": \"ONLINE\",
                  \"lat\": 37.5,
                  \"lng\": 127.0,
                  \"indoor\": true,
                  \"mood_tag\": 10
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @DisplayName("위도 범위를 벗어나면 400을 반환한다")
    void createCourseInvalidLat() throws Exception {
        String payload = """
                {
                  \"name\": \"Invalid Lat\",
                  \"category\": \"ONLINE\",
                  \"lat\": 123.0,
                  \"lng\": 127.0,
                  \"indoor\": true,
                  \"mood_tag\": 10
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lat"));
    }

    @Test
    @DisplayName("잘못된 카테고리 값이면 400을 반환한다")
    void createCourseInvalidCategory() throws Exception {
        String payload = """
                {
                  \"name\": \"Invalid Category\",
                  \"category\": \"UNKNOWN\",
                  \"lat\": 37.5,
                  \"lng\": 127.0,
                  \"indoor\": true,
                  \"mood_tag\": 10
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("잘못된 영업시간 형식이어도 무시하고 저장을 시도한다")
    void createCourseInvalidOpenHours() throws Exception {
        Mockito.when(courseService.create(any())).thenReturn(persistedCourse);

        String payload = """
                {
                  \"name\": \"Bad Hours\",
                  \"category\": \"ONLINE\",
                  \"lat\": 37.5,
                  \"lng\": 127.0,
                  \"indoor\": true,
                  \"mood_tag\": 10,
                  \"open_hours\": {
                    \"mon\": \"25:00-26:00\"
                  }
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
