package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieSummaryResponse(
        Long id,
        String title,
        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("vote_average")
        Double voteAverage,

        @JsonProperty("vote_count")
        Integer voteCount,

        Double popularity
) {
}
