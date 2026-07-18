package dev.alberto.moviecatalog.testdata.tmdb;

import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieSummaryResponse;
import org.instancio.Model;

import static org.instancio.Instancio.create;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

public final class TmdbMovieSummaryResponseMother {

    private static final Model<TmdbMovieSummaryResponse> VALID_MODEL =
            of(TmdbMovieSummaryResponse.class)
                    .generate(
                            field(TmdbMovieSummaryResponse.class, "id"),
                            generator -> generator.longs()
                                    .range(1L, 10_000_000L)
                    )
                    .generate(
                            field(
                                    TmdbMovieSummaryResponse.class,
                                    "voteAverage"
                            ),
                            generator -> generator.doubles()
                                    .range(0.0, 10.0)
                    )
                    .set(
                            field(
                                    TmdbMovieSummaryResponse.class,
                                    "releaseDate"
                            ),
                            "1999-10-15"
                    )
                    .set(
                            field(
                                    TmdbMovieSummaryResponse.class,
                                    "posterPath"
                            ),
                            "/poster.jpg"
                    )
                    .set(
                            field(
                                    TmdbMovieSummaryResponse.class,
                                    "backdropPath"
                            ),
                            "/backdrop.jpg"
                    )
                    .toModel();

    private TmdbMovieSummaryResponseMother() {
    }

    public static TmdbMovieSummaryResponse random() {
        return create(VALID_MODEL);
    }

    public static TmdbMovieSummaryResponse withId(Long movieId) {
        return of(VALID_MODEL)
                .set(
                        field(TmdbMovieSummaryResponse.class, "id"),
                        movieId
                )
                .create();
    }

    public static TmdbMovieSummaryResponse withoutPoster() {
        return of(VALID_MODEL)
                .set(
                        field(
                                TmdbMovieSummaryResponse.class,
                                "posterPath"
                        ),
                        null
                )
                .create();
    }

    public static TmdbMovieSummaryResponse withoutReleaseDate() {
        return of(VALID_MODEL)
                .set(
                        field(
                                TmdbMovieSummaryResponse.class,
                                "releaseDate"
                        ),
                        ""
                )
                .create();
    }
}
