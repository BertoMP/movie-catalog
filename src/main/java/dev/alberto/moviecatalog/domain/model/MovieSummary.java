package dev.alberto.moviecatalog.domain.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MovieSummary(
        Long id,
        String title,
        String overview,
        String posterUrl,
        LocalDate releaseDate,
        Double rating
) {
}
