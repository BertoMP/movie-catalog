package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

import lombok.Getter;

@Getter
public class TmdbRateLimitException extends TmdbException {

    private final Long retryAfterSeconds;

    public TmdbRateLimitException(
            String message,
            int upstreamStatus,
            Integer upstreamCode,
            String methodKey,
            Long retryAfterSeconds
    ) {
        super(
                message,
                upstreamStatus,
                upstreamCode,
                methodKey
        );

        this.retryAfterSeconds = retryAfterSeconds;
    }
}