package dev.alberto.moviecatalog.web.mapper;

import dev.alberto.moviecatalog.domain.model.MovieCollection;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieGenre;
import dev.alberto.moviecatalog.domain.model.MovieProductionCompany;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.web.response.MovieDetailResponse;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieApiMapperTest {

    private final MovieApiMapper mapper = new MovieApiMapper();

    @Test
    void shouldMapMovieSummaryToResponse() {
        MovieSummary source = MovieSummary.builder()
                .id(550L)
                .title("Fight Club")
                .overview("An insomniac office worker...")
                .posterUrl("https://image.tmdb.org/t/p/original/poster.jpg")
                .backdropUrl("https://image.tmdb.org/t/p/original/backdrop.jpg")
                .releaseDate(LocalDate.of(1999, 10, 15))
                .rating(8.4)
                .build();

        MovieSummaryResponse response = mapper.toResponse(source);

        assertThat(response.id()).isEqualTo(source.id());
        assertThat(response.title()).isEqualTo(source.title());
        assertThat(response.overview()).isEqualTo(source.overview());
        assertThat(response.posterUrl()).isEqualTo(source.posterUrl());
        assertThat(response.releaseDate()).isEqualTo(source.releaseDate());
        assertThat(response.rating()).isEqualTo(source.rating());
    }

    @Test
    void shouldMapMovieSummaryListToResponseList() {
        List<MovieSummary> source = List.of(
                MovieSummary.builder()
                        .id(550L)
                        .title("Fight Club")
                        .build(),
                MovieSummary.builder()
                        .id(13L)
                        .title("Forrest Gump")
                        .build()
        );

        List<MovieSummaryResponse> response = mapper.toResponse(source);

        assertThat(response)
                .extracting(MovieSummaryResponse::id)
                .containsExactly(550L, 13L);

        assertThat(response)
                .extracting(MovieSummaryResponse::title)
                .containsExactly("Fight Club", "Forrest Gump");
    }

    @Test
    void shouldMapMovieDetailToResponse() {
        MovieCollection collection = MovieCollection.builder()
                .id(10L)
                .name("Fight Club Collection")
                .posterUrl("collection-poster.jpg")
                .backdropUrl("collection-backdrop.jpg")
                .build();

        List<MovieGenre> genres = List.of(
                MovieGenre.builder()
                        .id(18L)
                        .name("Drama")
                        .build()
        );

        List<MovieProductionCompany> productionCompanies = List.of(
                MovieProductionCompany.builder()
                        .id(508L)
                        .logoPath("/logo.png")
                        .name("Regency Enterprises")
                        .build()
        );

        MovieDetail source = MovieDetail.builder()
                .id(550L)
                .title("Fight Club")
                .originalTitle("Fight Club")
                .overview("An insomniac office worker...")
                .posterUrl("https://image.tmdb.org/t/p/original/poster.jpg")
                .backdropUrl("https://image.tmdb.org/t/p/original/backdrop.jpg")
                .collection(collection)
                .releaseDate(LocalDate.of(1999, 10, 15))
                .runtime(139)
                .genres(genres)
                .tagline("Mischief. Mayhem. Soap.")
                .status("Released")
                .rating(8.4)
                .budget(63_000_000L)
                .revenue(100_853_753L)
                .productionCountries(List.of("United States of America"))
                .productionCompanies(productionCompanies)
                .build();

        MovieDetailResponse response = mapper.toResponse(source);

        assertThat(response.id()).isEqualTo(source.id());
        assertThat(response.title()).isEqualTo(source.title());
        assertThat(response.originalTitle()).isEqualTo(source.originalTitle());
        assertThat(response.overview()).isEqualTo(source.overview());
        assertThat(response.posterUrl()).isEqualTo(source.posterUrl());
        assertThat(response.backdropUrl()).isEqualTo(source.backdropUrl());
        assertThat(response.collection()).isEqualTo(source.collection());
        assertThat(response.releaseDate()).isEqualTo(source.releaseDate());
        assertThat(response.runtime()).isEqualTo(source.runtime());
        assertThat(response.genres()).isEqualTo(source.genres());
        assertThat(response.rating()).isEqualTo(source.rating());
        assertThat(response.budget()).isEqualTo(source.budget());
        assertThat(response.revenue()).isEqualTo(source.revenue());
        assertThat(response.productionCountries())
                .isEqualTo(source.productionCountries());
        assertThat(response.productionCompanies())
                .isEqualTo(source.productionCompanies());
    }
}
