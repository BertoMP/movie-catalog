package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieCollectionResponse(
        Long id,
        String name,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath
) {
}
