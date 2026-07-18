package dev.alberto.moviecatalog.testdata.tmdb;

import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieGenreResponse;

import static org.instancio.Instancio.create;

public final class TmdbGenreResponseMother {

    private TmdbGenreResponseMother() {
    }

    public static TmdbMovieGenreResponse random() {
        return create(TmdbMovieGenreResponse.class);
    }

    public static TmdbMovieGenreResponse withName(String name) {
        return new TmdbMovieGenreResponse(
                18L,
                name
        );
    }

    public static TmdbMovieGenreResponse drama() {
        return withName("Drama");
    }
}