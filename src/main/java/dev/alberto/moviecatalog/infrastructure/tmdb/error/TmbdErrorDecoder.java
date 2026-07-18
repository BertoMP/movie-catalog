package dev.alberto.moviecatalog.infrastructure.tmdb.error;

import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbErrorResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class TmbdErrorDecoder implements ErrorDecoder {

    private static final String DEFAULT_MESSAGE =
            "Unexpected error communicating with TMDB";

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(
            String methodKey,
            Response response
    ) {
        String body = readBody(response);
        TmdbErrorResponse tmdbError = parseError(body);

        String message = resolveMessage(tmdbError);
        Integer upstreamCode = tmdbError != null
                ? tmdbError.statusCode()
                : null;

        return switch (response.status()) {
            case 400, 422 -> new TmdbBadRequestException(
                    message,
                    response.status(),
                    upstreamCode,
                    methodKey
            );

            case 401 -> new TmdbAuthenticationException(
                    message,
                    response.status(),
                    upstreamCode,
                    methodKey
            );

            case 403 -> new TmdbForbiddenException(
                    message,
                    response.status(),
                    upstreamCode,
                    methodKey
            );

            case 404 -> new TmdbResourceNotFoundException(
                    message,
                    response.status(),
                    upstreamCode,
                    methodKey
            );

            case 429 -> new TmdbRateLimitException(
                    message,
                    response.status(),
                    upstreamCode,
                    methodKey,
                    readRetryAfter(response.headers())
            );

            default -> {
                if (response.status() >= 500) {
                    yield new TmdbServerException(
                            message,
                            response.status(),
                            upstreamCode,
                            methodKey
                    );
                }

                yield new TmdbUnexpectedException(
                        message,
                        response.status(),
                        upstreamCode,
                        methodKey
                );
            }
        };
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return null;
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private TmdbErrorResponse parseError(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, TmdbErrorResponse.class);
        } catch (JacksonException e) {
            return null;
        }
    }

    private String resolveMessage(TmdbErrorResponse error) {
        if (error == null
                || error.statusMessage() == null
                || error.statusMessage().isBlank()) {
            return DEFAULT_MESSAGE;
        }

        return error.statusMessage();
    }

    private Long readRetryAfter(
            Map<String, Collection<String>> headers
    ) {
        return headers.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey() != null
                                && entry.getKey()
                                .equalsIgnoreCase("Retry-After")
                )
                .flatMap(entry -> entry.getValue().stream())
                .findFirst()
                .map(this::parseLong)
                .orElse(null);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
