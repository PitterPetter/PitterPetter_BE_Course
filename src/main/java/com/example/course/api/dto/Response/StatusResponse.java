package com.example.course.api.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple status response")
public class StatusResponse {

    @Schema(description = "Operation result", example = "success")
    private final String status;

    private StatusResponse(String status) {
        this.status = status;
    }

    public static StatusResponse success() {
        return new StatusResponse("success");
    }

    public String getStatus() {
        return status;
    }
}
