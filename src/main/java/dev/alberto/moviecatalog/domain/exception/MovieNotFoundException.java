package dev.alberto.moviecatalog.domain.exception;

import lombok.Getter;

@Getter
public class MovieNotFoundException extends RuntimeException {
    private final Long movieId;

    public MovieNotFoundException(Long movieId) {
        super("Movie not found with id: " + movieId);
        this.movieId = movieId;
    }

    public MovieNotFoundException(
            Long movieId,
            Throwable cause
    ) {
        super("Movie not found with id: " + movieId, cause);
        this.movieId = movieId;
    }
}
