package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbAuthenticationException extends TmdbException {

    public TmdbAuthenticationException(
            String message,
            int upstreamStatus,
            Integer upstreamCode,
            String methodKey
    ) {
        super(
                message,
                upstreamStatus,
                upstreamCode,
                methodKey
        );
    }
}