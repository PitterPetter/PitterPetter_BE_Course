package com.example.course.api.controller;

import com.example.course.api.dto.Requset.CreateCourseRequest;
import com.example.course.api.dto.Requset.UpsertPoiReviewsRequest;
import com.example.course.api.dto.Response.CourseResponse;
import com.example.course.api.dto.Response.StatusResponse;
import com.example.course.service.CourseService;
import com.example.course.service.PoiReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
@Validated
@Tag(name = "Course", description = "Course management APIs")

public class CourseController {

    private final CourseService courseService;
    private final PoiReviewService poiReviewService;

    private static final String LOGIN_REQUIRED_MESSAGE = "로그인 후 진행해주세요.";

    public CourseController(CourseService courseService, PoiReviewService poiReviewService) {
        this.courseService = courseService;
        this.poiReviewService = poiReviewService;
    }

    @PostMapping(value = "/courses", consumes = "application/json")
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
                            value = "{\n  \"title\": \"한강 저녁 데이터 \",\n  \"explain\": \"오늘 무드에 맞는 코스입니다~ \",\n  \"data\": [\n    {\n      \"seq\": 1,\n      \"name\": \"Blue Bottle Yeonnam\",\n      \"category\": \"CAFE\",\n      \"lat\": 37.56231,\n      \"lng\": 126.92501,\n      \"indoor\": true,\n      \"priceLevel\": 2,\n      \"openHours\": {\n        \"mon\": \"09:00-18:00\"\n      },\n      \"alcohol\": 0,\n      \"moodTag\": 1001,\n      \"foodTag\": [\"COFFEE\", \"DESSERT\"],\n      \"ratingAvg\": 4.3\n    }\n  ]\n}"
                    )
            )
    )
    public StatusResponse createCourse(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        long coupleId = requireCoupleId(jwt);
        courseService.createCourse(coupleId, request);
        return StatusResponse.success();
    }

    @GetMapping("/courses")
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
                                    value = "[\n  {\n    \"courseId\": 1,\n    \"coupleId\": 5678,\n    \"title\": \"주말 데이트 코스\",\n    \"info\": \"서울숲 산책과 카페 방문 코스\",\n    \"score\": 10,\n    \"poiList\": [\n      {\n        \"poiSetId\": 11,\n        \"order\": 1,\n        \"rating\": null,\n        \"poi\": {\n          \"poiId\": 101,\n          \"name\": \"Blue Bottle Yeonnam\",\n          \"category\": \"CAFE\",\n          \"lat\": 37.56231,\n          \"lng\": 126.92501,\n          \"indoor\": true,\n          \"priceLevel\": 2,\n          \"openHours\": {\n            \"mon\": \"09:00-18:00\"\n          },\n          \"alcohol\": 0,\n          \"moodTag\": 1001,\n          \"foodTag\": [\"COFFEE\", \"DESSERT\"],\n          \"ratingAvg\": 4.3,\n          \"link\": \"https://example.com\"\n        }\n      }\n    ]\n  }\n]"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Courses not found for couple", content = @Content)
    })
    public List<CourseResponse> getCourses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        long coupleId = requireCoupleId(jwt);
        return courseService.findCoursesByCoupleId(coupleId)
                .stream()
                .map(CourseResponse::from)
                .toList();
    }

    @DeleteMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete course",
            description = "Deletes a course belonging to the authenticated couple. The course id is read from the URL path, and the couple id from the JWT claims.", // 설명 업데이트
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Course deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(
                                    name = "CourseDeleted",
                                    value = "{\n  \"status\": \"success\"\n}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content)
    })
    public StatusResponse deleteCourse(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long courseId
    ) {
        long coupleId = requireCoupleId(jwt);
        courseService.deleteCourse(coupleId, courseId);
        return StatusResponse.success();
    }

    @PostMapping(value = "/courses/reviews", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Bulk upsert POI reviews",
            description = "Upserts multiple POI reviews at once using the authenticated user's identity.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews upserted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(value = "{\n  \"status\": \"success\"\n}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public StatusResponse upsertReviews(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpsertPoiReviewsRequest request
    ) {
        long userId = requireUserId(jwt);
        requireCoupleId(jwt);
        List<PoiReviewService.ReviewCommand> commands = request.getData().stream()
                .map(item -> new PoiReviewService.ReviewCommand(item.getPoiId(), item.getRating()))
                .toList();
        poiReviewService.upsertReviews(userId, commands);
        return StatusResponse.success();
    }

    private long requireUserId(Jwt jwt) {
        return extractRequiredId(jwt, List.of("userId"));
    }

    private long requireCoupleId(Jwt jwt) {
        return extractRequiredId(jwt, List.of("coupleId"));
    }

    private long extractRequiredId(Jwt jwt, List<String> claimKeys) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_REQUIRED_MESSAGE);
        }
        Map<String, Object> claims = jwt.getClaims();
        for (String key : claimKeys) {
            Object raw = claims.get(key);
            Long value = toPositiveLong(raw);
            if (value != null) {
                return value;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_REQUIRED_MESSAGE);
    }

    private Long toPositiveLong(Object value) {
        if (value instanceof Number number) {
            long converted = number.longValue();
            return converted > 0 ? converted : null;
        }
        if (value instanceof String stringValue) {
            try {
                long parsed = Long.parseLong(stringValue);
                return parsed > 0 ? parsed : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
