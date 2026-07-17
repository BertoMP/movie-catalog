package dev.alberto.moviecatalog.application.exception;

import lombok.Getter;

@Getter
public class MovieProviderRateLimitedException
        extends MovieProviderException {

    private final Long retryAfterSeconds;

    public MovieProviderRateLimitedException(
            Long retryAfterSeconds,
            Throwable cause
    ) {
        super(
                "The movie provider rate limit has been exceeded",
                cause
        );

        this.retryAfterSeconds = retryAfterSeconds;
    }
}