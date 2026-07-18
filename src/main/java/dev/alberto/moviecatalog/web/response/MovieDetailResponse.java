package dev.alberto.moviecatalog.web.response;

import dev.alberto.moviecatalog.domain.model.MovieGenre;
import dev.alberto.moviecatalog.domain.model.MovieProductionCompany;
import dev.alberto.moviecatalog.domain.model.MovieCollection;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MovieDetailResponse(
        Long id,
        String title,
        String originalTitle,
        String overview,
        String posterUrl,
        String backdropUrl,
        MovieCollection collection,
        LocalDate releaseDate,
        Integer runtime,
        List<MovieGenre> genres,
        Double rating,
        Long revenue,
        Long budget,
        List<String> productionCountries,
        List<MovieProductionCompany> productionCompanies
) {
}
