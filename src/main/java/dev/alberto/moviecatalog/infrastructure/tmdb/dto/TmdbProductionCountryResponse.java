package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbProductionCountryResponse(
        @JsonProperty("iso_3166_1")
        String shortName,
        String name
) {
}
