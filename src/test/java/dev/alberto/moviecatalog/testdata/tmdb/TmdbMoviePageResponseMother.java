package dev.alberto.moviecatalog.testdata.tmdb;

import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieSummaryResponse;

import java.util.List;

public final class TmdbMoviePageResponseMother {

    private TmdbMoviePageResponseMother() {
    }

    public static TmdbMoviePageResponse random() {
        return withMovies(
                List.of(
                        TmdbMovieSummaryResponseMother.random(),
                        TmdbMovieSummaryResponseMother.random()
                )
        );
    }

    public static TmdbMoviePageResponse withMovies(
            List<TmdbMovieSummaryResponse> movies
    ) {
        return new TmdbMoviePageResponse(
                1,
                movies,
                1,
                movies.size()
        );
    }

    public static TmdbMoviePageResponse empty() {
        return withMovies(List.of());
    }
}