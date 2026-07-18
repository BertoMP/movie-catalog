package dev.alberto.moviecatalog.infrastructure.tmdb.mapper;

import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieSummaryResponse;
import dev.alberto.moviecatalog.testdata.tmdb
        .TmdbMovieDetailResponseMother;
import dev.alberto.moviecatalog.testdata.tmdb
        .TmdbMoviePageResponseMother;
import dev.alberto.moviecatalog.testdata.tmdb
        .TmdbMovieSummaryResponseMother;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(InstancioExtension.class)
class TmdbMovieMapperTest {

    private final TmdbMovieMapper mapper =
            new TmdbMovieMapper();

    @Test
    void shouldMapTrendingMoviesToDomain() {
        TmdbMovieSummaryResponse firstMovie =
                TmdbMovieSummaryResponseMother.random();

        TmdbMovieSummaryResponse secondMovie =
                TmdbMovieSummaryResponseMother.random();

        TmdbMoviePageResponse source =
                TmdbMoviePageResponseMother.withMovies(
                        List.of(firstMovie, secondMovie)
                );

        List<MovieSummary> result =
                mapper.toDomainList(source);

        assertThat(result).hasSize(2);

        assertThat(result.getFirst().id())
                .isEqualTo(firstMovie.id());

        assertThat(result.getFirst().title())
                .isEqualTo(firstMovie.title());

        assertThat(result.getFirst().overview())
                .isEqualTo(firstMovie.overview());

        assertThat(result.getFirst().rating())
                .isEqualTo(firstMovie.voteAverage());

        assertThat(result.getFirst().releaseDate())
                .isEqualTo(LocalDate.of(1999, 10, 15));

        assertThat(result.getFirst().posterUrl())
                .endsWith("/poster.jpg");

        assertThat(result.getFirst().backdropUrl())
                .endsWith("/backdrop.jpg");
    }

    @Test
    void shouldMapEmptyTrendingMovies() {
        TmdbMoviePageResponse source =
                TmdbMoviePageResponseMother.empty();

        List<MovieSummary> result =
                mapper.toDomainList(source);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapMovieDetailToDomain() {
        TmdbMovieDetailResponse source =
                TmdbMovieDetailResponseMother.random();

        MovieDetail result = mapper.toDomain(source);

        assertThat(result.id())
                .isEqualTo(source.id());

        assertThat(result.title())
                .isEqualTo(source.title());

        assertThat(result.overview())
                .isEqualTo(source.overview());

        assertThat(result.rating())
                .isEqualTo(source.voteAverage());

        assertThat(result.runtime())
                .isEqualTo(source.runtime());

        assertThat(result.releaseDate())
                .isEqualTo(LocalDate.of(1999, 10, 15));

        assertThat(result.posterUrl())
                .endsWith("/poster.jpg");

        assertThat(result.backdropUrl())
                .endsWith("/backdrop.jpg");

        assertThat(result.genres())
                .singleElement()
                .satisfies(genre -> {
                    assertThat(genre.id()).isEqualTo(source.genres().getFirst().id());
                    assertThat(genre.name()).isEqualTo("Drama");
                });
    }

    @Test
    void shouldHandleMissingOptionalMovieDetailFields() {
        TmdbMovieDetailResponse source =
                TmdbMovieDetailResponseMother
                        .withoutOptionalFields();

        MovieDetail result = mapper.toDomain(source);

        assertThat(result.posterUrl()).isNull();
        assertThat(result.backdropUrl()).isNull();
        assertThat(result.releaseDate()).isNull();
        assertThat(result.genres()).isEmpty();
    }
}
