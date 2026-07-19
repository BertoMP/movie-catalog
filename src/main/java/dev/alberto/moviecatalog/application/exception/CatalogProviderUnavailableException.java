package dev.alberto.moviecatalog.application.exception;

public class CatalogProviderUnavailableException
        extends CatalogProviderException {

    public CatalogProviderUnavailableException(Throwable cause) {
        super(
                "The catalog provider is currently unavailable",
                cause
        );
    }
}
