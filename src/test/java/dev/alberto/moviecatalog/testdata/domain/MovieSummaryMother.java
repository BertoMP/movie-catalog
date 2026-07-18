package dev.alberto.moviecatalog.testdata.domain;

import dev.alberto.moviecatalog.domain.model.MovieSummary;
import org.instancio.Model;

import java.util.List;
import java.util.stream.IntStream;

import static org.instancio.Instancio.create;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

public final class MovieSummaryMother {

    private static final Model<MovieSummary> VALID_MODEL =
            of(MovieSummary.class)
                    .generate(
                            field(MovieSummary.class, "id"),
                            generator -> generator.longs()
                                    .range(1L, 10_000_000L)
                    )
                    .generate(
                            field(MovieSummary.class, "rating"),
                            generator -> generator.doubles()
                                    .range(0.0, 10.0)
                    )
                    .toModel();

    private MovieSummaryMother() {
    }

    public static MovieSummary random() {
        return create(VALID_MODEL);
    }

    public static List<MovieSummary> randomList(int size) {
        return IntStream.range(0, size)
                .mapToObj(index -> random())
                .toList();
    }

    public static MovieSummary withId(Long movieId) {
        return of(VALID_MODEL)
                .set(
                        field(MovieSummary.class, "id"),
                        movieId
                )
                .create();
    }

    public static MovieSummary withTitle(String title) {
        return of(VALID_MODEL)
                .set(
                        field(MovieSummary.class, "title"),
                        title
                )
                .create();
    }
}
