package dev.alberto.moviecatalog.infrastructure.tmdb.error;

import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbAuthenticationException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbBadRequestException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbForbiddenException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbRateLimitException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbResourceNotFoundException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbServerException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbUnexpectedException;
import dev.alberto.moviecatalog.testdata.feign
        .FeignResponseMother;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TmdbErrorDecoderTest {

    private static final String METHOD_KEY =
            "TmdbFeignClient#getMovieById";

    private static final String ERROR_BODY = """
            {
              "success": false,
              "status_code": 34,
              "status_message": "Test error"
            }
            """;

    private final ObjectMapper objectMapper =
            JsonMapper.builder().build();

    private final TmbdErrorDecoder decoder =
            new TmbdErrorDecoder(objectMapper);

    @ParameterizedTest
    @ValueSource(ints = {400, 422})
    void shouldDecodeInvalidRequest(int status) {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        status,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbBadRequestException.class
                );
    }

    @Test
    void shouldDecodeAuthenticationError() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        401,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbAuthenticationException.class
                );
    }

    @Test
    void shouldDecodeForbiddenError() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        403,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbForbiddenException.class
                );
    }

    @Test
    void shouldDecodeResourceNotFound() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        404,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbResourceNotFoundException.class
                );

        TmdbResourceNotFoundException exception =
                (TmdbResourceNotFoundException) result;

        assertThat(exception.getUpstreamStatus())
                .isEqualTo(404);

        assertThat(exception.getUpstreamCode())
                .isEqualTo(34);

        assertThat(exception.getMethodKey())
                .isEqualTo(METHOD_KEY);

        assertThat(exception.getMessage())
                .isEqualTo("Test error");
    }

    @Test
    void shouldDecodeRateLimitAndReadRetryAfter() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withRetryAfter(
                        429,
                        ERROR_BODY,
                        60L
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbRateLimitException.class
                );

        TmdbRateLimitException exception =
                (TmdbRateLimitException) result;

        assertThat(exception.getRetryAfterSeconds())
                .isEqualTo(60L);
    }

    @Test
    void shouldIgnoreInvalidRetryAfter() {
        Response response =
                FeignResponseMother.withHeaders(
                        429,
                        ERROR_BODY,
                        Map.of(
                                "Retry-After",
                                List.of("not-a-number")
                        )
                );

        TmdbRateLimitException result =
                (TmdbRateLimitException) decoder.decode(
                        METHOD_KEY,
                        response
                );

        assertThat(result.getRetryAfterSeconds())
                .isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void shouldDecodeServerError(int status) {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        status,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbServerException.class
                );
    }

    @Test
    void shouldDecodeUnexpectedStatus() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withStatusAndBody(
                        418,
                        ERROR_BODY
                )
        );

        assertThat(result)
                .isExactlyInstanceOf(
                        TmdbUnexpectedException.class
                );
    }

    @Test
    void shouldUseDefaultMessageWhenBodyIsMissing() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withoutBody(503)
        );

        assertThat(result)
                .isInstanceOf(TmdbServerException.class)
                .hasMessage(
                        "Unexpected error communicating with TMDB"
                );
    }

    @Test
    void shouldUseDefaultMessageWhenBodyIsMalformed() {
        Exception result = decoder.decode(
                METHOD_KEY,
                FeignResponseMother.withMalformedBody(503)
        );

        assertThat(result)
                .isInstanceOf(TmdbServerException.class)
                .hasMessage(
                        "Unexpected error communicating with TMDB"
                );
    }
}