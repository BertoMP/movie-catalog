package dev.alberto.moviecatalog.domain.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MovieDetail(
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
        String tagline,
        String status,
        Double rating,
        Long budget,
        Long revenue,
        List<String> productionCountries,
        List<MovieProductionCompany> productionCompanies

) {
}
