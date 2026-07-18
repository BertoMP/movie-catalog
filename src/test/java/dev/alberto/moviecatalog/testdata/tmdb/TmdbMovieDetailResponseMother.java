package dev.alberto.moviecatalog.testdata.tmdb;

import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieDetailResponse;
import org.instancio.Model;

import java.util.List;

import static org.instancio.Instancio.create;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

public final class TmdbMovieDetailResponseMother {

    private static final Model<TmdbMovieDetailResponse> VALID_MODEL =
            of(TmdbMovieDetailResponse.class)
                    .generate(
                            field(TmdbMovieDetailResponse.class, "id"),
                            generator -> generator.longs()
                                    .range(1L, 10_000_000L)
                    )
                    .generate(
                            field(
                                    TmdbMovieDetailResponse.class,
                                    "voteAverage"
                            ),
                            generator -> generator.doubles()
                                    .range(0.0, 10.0)
                    )
                    .generate(
                            field(TmdbMovieDetailResponse.class, "runtime"),
                            generator -> generator.ints()
                                    .range(1, 400)
                    )
                    .set(
                            field(
                                    TmdbMovieDetailResponse.class,
                                    "releaseDate"
                            ),
                            "1999-10-15"
                    )
                    .set(
                            field(
                                    TmdbMovieDetailResponse.class,
                                    "posterPath"
                            ),
                            "/poster.jpg"
                    )
                    .set(
                            field(
                                    TmdbMovieDetailResponse.class,
                                    "backdropPath"
                            ),
                            "/backdrop.jpg"
                    )
                    .set(
                            field(TmdbMovieDetailResponse.class, "genres"),
                            List.of(TmdbGenreResponseMother.drama())
                    )
                    .toModel();

    private TmdbMovieDetailResponseMother() {
    }

    public static TmdbMovieDetailResponse random() {
        return create(VALID_MODEL);
    }

    public static TmdbMovieDetailResponse withId(Long movieId) {
        return of(VALID_MODEL)
                .set(
                        field(TmdbMovieDetailResponse.class, "id"),
                        movieId
                )
                .create();
    }

    public static TmdbMovieDetailResponse withoutOptionalFields() {
        return of(VALID_MODEL)
                .set(
                        field(TmdbMovieDetailResponse.class, "posterPath"),
                        null
                )
                .set(
                        field(TmdbMovieDetailResponse.class, "backdropPath"),
                        null
                )
                .set(
                        field(TmdbMovieDetailResponse.class, "releaseDate"),
                        ""
                )
                .set(
                        field(TmdbMovieDetailResponse.class, "genres"),
                        null
                )
                .create();
    }
}
