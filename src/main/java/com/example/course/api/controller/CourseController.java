package com.example.course.api.controller;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.api.dto.Response.CourseResponse;
import com.example.course.api.dto.Response.StatusResponse;
import com.example.course.jwt.JwtProvider;
import com.example.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/courses", produces = "application/json")
@Validated
@Tag(name = "Course", description = "Course management APIs")
public class CourseController {

    private final CourseService courseService;
    private final JwtProvider jwtProvider;

    public CourseController(CourseService courseService, JwtProvider jwtProvider) {
        this.courseService = courseService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new course",
            description = "Creates a course for the authenticated couple.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Course created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(
                                    name = "CourseCreated",
                                    value = "{\n  \"status\": \"success\"\n}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Course creation payload",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateCourseRequest.class),
                    examples = @ExampleObject(
                            name = "CreateCourse",
                            value = "{\n  \"title\": \"한강 저녁 데이터 \",\n  \"explain\": \"오늘 무드에 맞는 코스입니다~ \",\n  \"data\": [\n    {\n      \"seq\": 1,\n      \"name\": \"Blue Bottle Yeonnam\",\n      \"category\": \"CAFE\",\n      \"lat\": 37.56231,\n      \"lng\": 126.92501,\n      \"indoor\": true,\n      \"price_level\": 2,\n      \"open_hours\": {\n        \"mon\": \"09:00-18:00\"\n      },\n      \"alcohol\": 0,\n      \"mood_tag\": 1001,\n      \"food_tag\": [\"COFFEE\", \"DESSERT\"],\n      \"rating_avg\": 4.3\n    }\n  ]\n}"
                    )
            )
    )
    public StatusResponse createCourse(
            @Parameter(
                    name = HttpHeaders.AUTHORIZATION,
                    description = "Bearer token received from the gateway",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Long coupleId = jwtProvider.extractCoupleId(authorizationHeader);
        courseService.createCourse(coupleId, request);
        return StatusResponse.success();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "List courses",
            description = "Returns all courses for the authenticated couple including their POI details.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Courses retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CourseResponse.class)),
                            examples = @ExampleObject(
                                    name = "Courses",
                                    value = "[\n  {\n    \"course_id\": 1,\n    \"couple_id\": 5678,\n    \"title\": \"주말 데이트 코스\",\n    \"info\": \"서울숲 산책과 카페 방문 코스\",\n    \"score\": 10,\n    \"poi_list\": [\n      {\n        \"poi_set_id\": 11,\n        \"order\": 1,\n        \"rating\": null,\n        \"poi\": {\n          \"poi_id\": 101,\n          \"name\": \"Blue Bottle Yeonnam\",\n          \"category\": \"CAFE\",\n          \"lat\": 37.56231,\n          \"lng\": 126.92501,\n          \"indoor\": true,\n          \"price_level\": 2,\n          \"open_hours\": {\n            \"mon\": \"09:00-18:00\"\n          },\n          \"alcohol\": 0,\n          \"mood_tag\": 1001,\n          \"food_tag\": [\"COFFEE\", \"DESSERT\"],\n          \"rating_avg\": 4.3,\n          \"link\": \"https://example.com\"\n        }\n      }\n    ]\n  }\n]"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Courses not found for couple", content = @Content)
    })
    public List<CourseResponse> getCourses(
            @Parameter(
                    name = HttpHeaders.AUTHORIZATION,
                    description = "Bearer token received from the gateway",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        Long coupleId = jwtProvider.extractCoupleId(authorizationHeader);
        return courseService.findCoursesByCoupleId(coupleId)
                .stream()
                .map(CourseResponse::from)
                .toList();
    }
}
