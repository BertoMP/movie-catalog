package dev.alberto.moviecatalog.testdata.domain;

import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieGenre;
import org.instancio.Model;

import java.util.List;

import static org.instancio.Instancio.create;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

public final class MovieDetailMother {

    private static final Model<MovieDetail> VALID_MODEL =
            of(MovieDetail.class)
                    .generate(
                            field(MovieDetail.class, "id"),
                            generator -> generator.longs()
                                    .range(1L, 10_000_000L)
                    )
                    .generate(
                            field(MovieDetail.class, "rating"),
                            generator -> generator.doubles()
                                    .range(0.0, 10.0)
                    )
                    .generate(
                            field(MovieDetail.class, "runtime"),
                            generator -> generator.ints()
                                    .range(1, 400)
                    )
                    .set(
                            field(MovieDetail.class, "genres"),
                            List.of(
                                    MovieGenre.builder()
                                            .id(18L)
                                            .name("Drama")
                                            .build(),
                                    MovieGenre.builder()
                                            .id(53L)
                                            .name("Thriller")
                                            .build()
                            )
                    )
                    .toModel();

    private MovieDetailMother() {
    }

    public static MovieDetail random() {
        return create(VALID_MODEL);
    }

    public static MovieDetail withId(Long movieId) {
        return of(VALID_MODEL)
                .set(
                        field(MovieDetail.class, "id"),
                        movieId
                )
                .create();
    }
}
