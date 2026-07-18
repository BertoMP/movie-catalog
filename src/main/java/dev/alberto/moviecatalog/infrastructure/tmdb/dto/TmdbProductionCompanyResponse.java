package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbProductionCompanyResponse(
        Long id,

        @JsonProperty("logo_path")
        String logoPath,

        String name
) {
}
