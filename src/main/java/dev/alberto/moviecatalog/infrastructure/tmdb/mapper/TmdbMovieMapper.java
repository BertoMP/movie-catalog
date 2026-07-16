package dev.alberto.moviecatalog.infrastructure.tmdb.mapper;

import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieSummaryResponse;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class TmdbMovieMapper {

    private static final String IMAGE_BASE_URL =
            "https://image.tmdb.org/t/p/w500";

    public List<MovieSummary> toDomainList(TmdbMoviePageResponse source) {
        return source.results() == null
                ? List.of()
                : source.results()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MovieSummary toDomain(
            TmdbMovieSummaryResponse source
    ) {
        return new MovieSummary(
                source.id(),
                source.title(),
                source.overview(),
                buildPosterUrl(source.posterPath()),
                parseReleaseDate(source.releaseDate()),
                source.voteAverage()
        );
    }

    private String buildPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        return IMAGE_BASE_URL + posterPath;
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
