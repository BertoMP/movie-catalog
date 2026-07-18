package dev.alberto.moviecatalog.application.exception;

public abstract class MovieProviderException extends RuntimeException {

    protected MovieProviderException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}