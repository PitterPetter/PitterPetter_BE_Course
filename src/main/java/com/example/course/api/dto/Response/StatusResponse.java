package com.example.course.api.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple status response")
public record StatusResponse(
    @Schema(description = "Operation result", example = "success")
    String status
) {
    public static StatusResponse success() {
        return new StatusResponse("success");
    }
}