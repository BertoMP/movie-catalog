package dev.alberto.moviecatalog.application.exception;

public abstract class CatalogProviderException extends RuntimeException {

    protected CatalogProviderException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}