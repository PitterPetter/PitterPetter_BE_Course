package com.example.poi.api;

import com.example.poi.api.dto.CourseResponse;
import com.example.poi.api.dto.CreateCourseRequest;
import com.example.poi.domain.Course;
import com.example.poi.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@Validated
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        Course course = courseService.create(request);
        return ApiResponse.success(CourseResponse.from(course));
    }
}
