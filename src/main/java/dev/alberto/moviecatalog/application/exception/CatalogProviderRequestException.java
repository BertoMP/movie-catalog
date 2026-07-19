package dev.alberto.moviecatalog.application.exception;

public class CatalogProviderRequestException
        extends CatalogProviderException {

    public CatalogProviderRequestException(Throwable cause) {
        super(
                "The catalog provider rejected the request",
                cause
        );
    }
}
