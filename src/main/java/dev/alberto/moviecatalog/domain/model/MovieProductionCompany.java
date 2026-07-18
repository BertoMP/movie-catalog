package dev.alberto.moviecatalog.domain.model;

import lombok.Builder;

@Builder
public record MovieProductionCompany(
        Long id,
        String logoPath,
        String name
) {
}