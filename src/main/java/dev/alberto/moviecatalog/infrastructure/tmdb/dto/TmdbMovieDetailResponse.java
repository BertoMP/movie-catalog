package dev.alberto.moviecatalog.infrastructure.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieDetailResponse(
        Long id,
        String title,

        @JsonProperty("original_title")
        String originalTitle,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("belongs_to_collection")
        TmdbMovieCollectionResponse belongsToCollection,

        @JsonProperty("release_date")
        String releaseDate,

        Integer runtime,
        List<TmdbMovieGenreResponse> genres,

        @JsonProperty("vote_average")
        Double voteAverage,

        Long budget,
        Long revenue,

        @JsonProperty("production_countries")
        List<TmdbProductionCountryResponse> productionCountries,

        @JsonProperty("production_companies")
        List<TmdbProductionCompanyResponse> productionCompanies
) {
}
