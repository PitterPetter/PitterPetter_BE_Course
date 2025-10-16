package com.example.course.api.dto.Response;

import com.example.course.domain.Course;
import com.example.course.domain.Poi;
import com.example.course.domain.PoiSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "Course representation")
public record CourseResponse(
    @Schema(description = "Identifier of the course", example = "\"1\"")
    String courseId,
    
    @Schema(description = "Course title", example = "주말 데이트 코스")
    String title,
    
    @Schema(description = "Course description", example = "서울숲 산책과 카페 방문 코스")
    String description,
    
    @Schema(description = "Course score", example = "10")
    Long score,
    
    @Schema(description = "Ordered list of POIs that compose the course")
    List<PoiSetResponse> poiList
) {
    public static CourseResponse from(Course course) {
        List<PoiSetResponse> poiList = course.getPoiSets() == null
                ? Collections.emptyList()
                : course.getPoiSets().stream()
                .map(PoiSetResponse::from)
                .collect(Collectors.toList());
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getScore(),
                poiList
        );
    }

    @Schema(description = "POI set within a course")
    public record PoiSetResponse(
        @Schema(description = "Identifier of the course-to-poi association", example = "11")
        Long poiSetId,
        
        @Schema(description = "Order of the POI within the course", example = "1")
        Integer order,
        
        @Schema(description = "Detailed POI information")
        PoiResponse poi
    ) {
        private static PoiSetResponse from(PoiSet poiSet) {
            Poi poi = poiSet.getPoi();
            return new PoiSetResponse(
                    poiSet.getId(),
                    poiSet.getOrderIndex(),
                    poi != null ? PoiResponse.from(poi) : null
            );
        }
    }

    @Schema(description = "POI details")
    public record PoiResponse(
        @Schema(description = "Identifier of the POI", example = "101")
        Long poiId,
        
        @Schema(description = "POI name", example = "Blue Bottle Yeonnam")
        String name,
        
        @Schema(description = "POI category", example = "CAFE")
        String category,
        
        @Schema(description = "Latitude", example = "37.56231")
        Double lat,
        
        @Schema(description = "Longitude", example = "126.92501")
        Double lng,
        
        @Schema(description = "Indoor availability", example = "true")
        Boolean indoor,
        
        @Schema(description = "Price level", example = "2")
        Integer priceLevel,
        
        @Schema(description = "Opening hours by day", example = "{\"mon\":\"09:00-18:00\"}")
        Map<String, String> openHours,
        
        @Schema(description = "Alcohol availability", example = "1")
        Integer alcohol,
        
        @Schema(description = "Mood tag identifier in camelCase", example = "warmVibes")
        String moodTag,
        
        @Schema(description = "Food tags", example = "[\"coffee\", \"dessert\"]")
        List<String> foodTag,
        
        @Schema(description = "External link", example = "https://example.com")
        String link,
        
        @Schema(description = "Average rating", example = "4.3")
        Double ratingAvg
    ) {
        private static PoiResponse from(Poi poi) {
            return new PoiResponse(
                    poi.getId(),
                    poi.getName(),
                    poi.getCategory() != null ? poi.getCategory().name() : null,
                    poi.getLat(),
                    poi.getLng(),
                    poi.getIndoor(),
                    poi.getPriceLevel(),
                    poi.getOpenHours(),
                    poi.getAlcohol(),
                    poi.getMoodTag(),
                    poi.getFoodTag() != null ? List.copyOf(poi.getFoodTag()) : List.of(),
                    poi.getLink(),
                    poi.getRatingAvg()
            );
        }
    }
}