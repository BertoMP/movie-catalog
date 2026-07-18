package dev.alberto.moviecatalog.infrastructure.tmdb.mapper;

import dev.alberto.moviecatalog.domain.model.MovieCollection;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieGenre;
import dev.alberto.moviecatalog.domain.model.MovieProductionCompany;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.*;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class TmdbMovieMapper {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public List<MovieSummary> toDomainList(TmdbMoviePageResponse source) {
        return source.results() == null
                ? List.of()
                : source.results()
                .stream()
                .map(this::toMovieSummary)
                .toList();
    }

    private MovieSummary toMovieSummary(
            TmdbMovieSummaryResponse source
    ) {
        return MovieSummary.builder()
                .id(source.id())
                .title(source.title())
                .overview(source.overview())
                .posterUrl(buildImageUrl(source.posterPath()))
                .releaseDate(parseReleaseDate(source.releaseDate()))
                .rating(source.voteAverage())
                .build();
    }

    public MovieDetail toDomain(TmdbMovieDetailResponse source) {
        return MovieDetail.builder()
                .id(source.id())
                .title(source.title())
                .originalTitle(source.originalTitle())
                .overview(source.overview())
                .posterUrl(buildImageUrl(source.posterPath()))
                .backdropUrl(buildImageUrl(source.backdropPath()))
                .collection(toMovieCollection(source.belongsToCollection()))
                .releaseDate(parseReleaseDate(source.releaseDate()))
                .runtime(source.runtime())
                .genres(toMovieGenres(source.genres()))
                .rating(source.voteAverage())
                .budget(source.budget())
                .revenue(source.revenue())
                .productionCountries(toProductionCountryNames(source.productionCountries()))
                .productionCompanies(toProductionCompanies(source.productionCompanies()))
                .build();
    }

    private MovieCollection toMovieCollection(TmdbMovieCollectionResponse source) {
        if (source == null) {
            return null;
        }

        return MovieCollection.builder()
                .id(source.id())
                .name(source.name())
                .posterUrl(buildImageUrl(source.posterPath()))
                .backdropUrl(buildImageUrl(source.backdropPath()))
                .build();
    }

    private List<MovieGenre> toMovieGenres(List<TmdbMovieGenreResponse> source) {
        return source == null
                ? List.of()
                : source.stream()
                .map(this::toMovieGenre)
                .toList();
    }

    private List<String> toProductionCountryNames(List<TmdbProductionCountryResponse> source) {
        return source == null
                ? List.of()
                : source.stream()
                .map(TmdbProductionCountryResponse::name)
                .toList();
    }

    private List<MovieProductionCompany> toProductionCompanies(List<TmdbProductionCompanyResponse> source) {
        return source == null
                ? List.of()
                : source.stream()
                .map(this::toProductionCompany)
                .toList();
    }

    private MovieProductionCompany toProductionCompany(TmdbProductionCompanyResponse source) {
        return MovieProductionCompany.builder()
                .id(source.id())
                .logoPath(buildImageUrl(source.logoPath()))
                .name(source.name())
                .build();
    }

    private MovieGenre toMovieGenre(TmdbMovieGenreResponse source) {
        return MovieGenre.builder()
                .id(source.id())
                .name(source.name())
                .build();
    }

    private String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        return IMAGE_BASE_URL + imagePath;
    }

    private LocalDate parseReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(releaseDate);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
