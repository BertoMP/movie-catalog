package dev.alberto.moviecatalog.infrastructure.tmdb.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.TmdbAuthenticationException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.TmdbRateLimitException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.TmdbResourceNotFoundException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.TmdbServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@EnableWireMock(
        @ConfigureWireMock(
                name = "tmdb",
                baseUrlProperties = "wiremock.tmdb.base-url"
        )
)
class TmdbFeignClientIntegrationTest {

    private static final String LANGUAGE = "en-US";
    private static final String ACCESS_TOKEN = "test-token";

    @Autowired
    private TmdbFeignClient tmdbFeignClient;

    @InjectWireMock("tmdb")
    private WireMockServer tmdbWireMock;

    @BeforeEach
    void setUp() {
        tmdbWireMock.resetAll();
    }

    @Test
    void shouldGetTrendingMovies() {
        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/trending/movie/day"))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                equalTo("Bearer " + ACCESS_TOKEN)
                        )
                        .withHeader(
                                HttpHeaders.ACCEPT,
                                containing(MediaType.APPLICATION_JSON_VALUE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(
                                                "tmdb/trending-movies-response.json"
                                        )
                        )
        );

        TmdbMoviePageResponse result =
                tmdbFeignClient.getTrendingMovies(
                        "day",
                        LANGUAGE
                );

        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.results()).hasSize(1);

        assertThat(result.results().getFirst().id())
                .isEqualTo(550L);

        assertThat(result.results().getFirst().title())
                .isEqualTo("Fight Club");

        tmdbWireMock.verify(
                exactly(1),
                getRequestedFor(
                        urlPathEqualTo(
                                "/3/trending/movie/day"
                        )
                )
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                equalTo("Bearer " + ACCESS_TOKEN)
                        )
                        .withHeader(
                                HttpHeaders.ACCEPT,
                                containing(MediaType.APPLICATION_JSON_VALUE)
                        )
        );
    }

    @Test
    void shouldGetMovieDetail() {
        long movieId = 550L;

        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/movie/" + movieId))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                equalTo("Bearer " + ACCESS_TOKEN)
                        )
                        .withHeader(
                                HttpHeaders.ACCEPT,
                                containing(MediaType.APPLICATION_JSON_VALUE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(
                                                "tmdb/movie-detail-response.json"
                                        )
                        )
        );

        TmdbMovieDetailResponse result =
                tmdbFeignClient.getMovieById(
                        movieId,
                        LANGUAGE
                );

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(movieId);
        assertThat(result.title()).isEqualTo("Fight Club");
        assertThat(result.runtime()).isEqualTo(139);

        assertThat(result.genres())
                .extracting(genre -> genre.name())
                .containsExactly("Drama");

        tmdbWireMock.verify(
                exactly(1),
                getRequestedFor(
                        urlPathEqualTo(
                                "/3/movie/" + movieId
                        )
                )
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                equalTo("Bearer " + ACCESS_TOKEN)
                        )
                        .withHeader(
                                HttpHeaders.ACCEPT,
                                containing(MediaType.APPLICATION_JSON_VALUE)
                        )
        );
    }

    @Test
    void shouldDecodeAuthenticationError() {
        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/trending/movie/day"))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(
                                                "tmdb/authentication-error-response.json"
                                        )
                        )
        );

        assertThatThrownBy(
                () -> tmdbFeignClient.getTrendingMovies(
                        "day",
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        TmdbAuthenticationException.class
                )
                .satisfies(exception -> {
                    TmdbAuthenticationException tmdbException =
                            (TmdbAuthenticationException) exception;

                    assertThat(tmdbException.getUpstreamStatus())
                            .isEqualTo(401);

                    assertThat(tmdbException.getUpstreamCode())
                            .isEqualTo(7);
                });
    }

    @Test
    void shouldDecodeMovieNotFoundError() {
        long movieId = 999_999_999L;

        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/movie/" + movieId))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(
                                                "tmdb/movie-not-found-response.json"
                                        )
                        )
        );

        assertThatThrownBy(
                () -> tmdbFeignClient.getMovieById(
                        movieId,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        TmdbResourceNotFoundException.class
                )
                .satisfies(exception -> {
                    TmdbResourceNotFoundException tmdbException =
                            (TmdbResourceNotFoundException) exception;

                    assertThat(tmdbException.getUpstreamStatus())
                            .isEqualTo(404);

                    assertThat(tmdbException.getUpstreamCode())
                            .isEqualTo(34);
                });
    }

    @Test
    void shouldDecodeRateLimitAndReadRetryAfter() {
        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/trending/movie/week"))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(429)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withHeader(
                                                HttpHeaders.RETRY_AFTER,
                                                "60"
                                        )
                                        .withBodyFile(
                                                "tmdb/rate-limit-response.json"
                                        )
                        )
        );

        assertThatThrownBy(
                () -> tmdbFeignClient.getTrendingMovies(
                        "week",
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        TmdbRateLimitException.class
                )
                .satisfies(exception -> {
                    TmdbRateLimitException tmdbException =
                            (TmdbRateLimitException) exception;

                    assertThat(tmdbException.getUpstreamStatus())
                            .isEqualTo(429);

                    assertThat(tmdbException.getUpstreamCode())
                            .isEqualTo(25);

                    assertThat(tmdbException.getRetryAfterSeconds())
                            .isEqualTo(60L);
                });
    }

    @Test
    void shouldDecodeServerError() {
        long movieId = 550L;

        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/movie/" + movieId))
                        .withQueryParam(
                                "language",
                                equalTo(LANGUAGE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(503)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(
                                                "tmdb/server-error-response.json"
                                        )
                        )
        );

        assertThatThrownBy(
                () -> tmdbFeignClient.getMovieById(
                        movieId,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        TmdbServerException.class
                )
                .satisfies(exception -> {
                    TmdbServerException tmdbException =
                            (TmdbServerException) exception;

                    assertThat(tmdbException.getUpstreamStatus())
                            .isEqualTo(503);

                    assertThat(tmdbException.getUpstreamCode())
                            .isEqualTo(11);
                });
    }
}
