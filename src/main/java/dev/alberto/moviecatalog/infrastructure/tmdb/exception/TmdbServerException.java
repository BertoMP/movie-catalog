package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbServerException extends TmdbException {

    public TmdbServerException(
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