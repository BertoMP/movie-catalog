package dev.alberto.moviecatalog.web.mapper;

import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.web.response.MovieDetailResponse;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieApiMapper {

    public List<MovieSummaryResponse> toResponse(List<MovieSummary> source) {
        return source.stream()
                .map(this::toResponse)
                .toList();
    }

    public MovieDetailResponse toResponse(MovieDetail source) {
        return MovieDetailResponse.builder()
                .id(source.id())
                .title(source.title())
                .originalTitle(source.originalTitle())
                .overview(source.overview())
                .posterUrl(source.posterUrl())
                .backdropUrl(source.backdropUrl())
                .collection(source.collection())
                .releaseDate(source.releaseDate())
                .runtime(source.runtime())
                .genres(source.genres())
                .rating(source.rating())
                .budget(source.budget())
                .revenue(source.revenue())
                .productionCountries(source.productionCountries())
                .productionCompanies(source.productionCompanies())
                .build();
    }

    public MovieSummaryResponse toResponse(MovieSummary source) {
        return MovieSummaryResponse.builder()
                .id(source.id())
                .title(source.title())
                .overview(source.overview())
                .posterUrl(source.posterUrl())
                .releaseDate(source.releaseDate())
                .rating(source.rating())
                .build();
    }
}
