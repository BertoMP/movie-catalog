package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbErrorResponse(
        Boolean success,

        @JsonProperty("status_code")
        Integer statusCode,

        @JsonProperty("status_message")
        String statusMessage
) {
}
