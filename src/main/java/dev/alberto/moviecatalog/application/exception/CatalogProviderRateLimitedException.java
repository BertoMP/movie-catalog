package dev.alberto.moviecatalog.application.exception;

import lombok.Getter;

@Getter
public class CatalogProviderRateLimitedException
        extends CatalogProviderException {

    private final Long retryAfterSeconds;

    public CatalogProviderRateLimitedException(
            Long retryAfterSeconds,
            Throwable cause
    ) {
        super(
                "The catalog provider rate limit has been exceeded",
                cause
        );

        this.retryAfterSeconds = retryAfterSeconds;
    }
}
