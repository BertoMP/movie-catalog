package dev.alberto.moviecatalog.acceptance;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@EnableWireMock(
        @ConfigureWireMock(
                name = "tmdb",
                baseUrlProperties = "wiremock.tmdb.base-url"
        )
)
class MovieCatalogAcceptanceTest {

    private static final String DEFAULT_LANGUAGE = "en-US";
    private static final String SPANISH_LANGUAGE = "es-ES";
    private static final String UNSUPPORTED_LANGUAGE = "pt-BR";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @InjectWireMock("tmdb")
    private WireMockServer tmdbWireMock;

    @BeforeEach
    void setUp() {
        tmdbWireMock.resetAll();
    }

    @Test
    void shouldReturnTrendingMovies()
            throws Exception {

        stubTmdbJson(
                "/3/trending/movie/day",
                200,
                "tmdb/trending-movies-response.json"
        );

        HttpResponse<String> response =
                executeGet("/api/v1/movies/trending");

        assertThat(response.statusCode())
                .isEqualTo(200);

        assertThat(response.body())
                .contains("\"id\":550")
                .contains("\"title\":\"Fight Club\"")
                .contains("\"posterUrl\":\"https://image.tmdb.org/t/p/original/poster.jpg\"")
                .contains("\"rating\":8.4");
    }

    @Test
    void shouldReturnMovieDetail()
            throws Exception {

        stubTmdbJson(
                "/3/movie/550",
                200,
                "tmdb/movie-detail-response.json"
        );

        HttpResponse<String> response =
                executeGet("/api/v1/movies/550");

        assertThat(response.statusCode())
                .isEqualTo(200);

        assertThat(response.body())
                .contains("\"id\":550")
                .contains("\"title\":\"Fight Club\"")
                .contains("\"runtime\":139")
                .contains("\"genres\":[{\"id\":18,\"name\":\"Drama\"}]")
                .contains("\"posterUrl\":\"https://image.tmdb.org/t/p/original/poster.jpg\"");
    }

    @Test
    void shouldUseAcceptLanguageHeader()
            throws Exception {

        stubTmdbJson(
                "/3/movie/550",
                SPANISH_LANGUAGE,
                200,
                "tmdb/movie-detail-response.json"
        );

        HttpResponse<String> response =
                executeGet(
                        "/api/v1/movies/550",
                        SPANISH_LANGUAGE
                );

        assertThat(response.statusCode())
                .isEqualTo(200);

        assertThat(response.body())
                .contains("\"id\":550")
                .contains("\"title\":\"Fight Club\"");
    }

    @Test
    void shouldUseDefaultLanguageWhenAcceptLanguageIsNotSupported()
            throws Exception {

        stubTmdbJson(
                "/3/movie/550",
                DEFAULT_LANGUAGE,
                200,
                "tmdb/movie-detail-response.json"
        );

        HttpResponse<String> response =
                executeGet(
                        "/api/v1/movies/550",
                        UNSUPPORTED_LANGUAGE
                );

        assertThat(response.statusCode())
                .isEqualTo(200);

        assertThat(response.body())
                .contains("\"id\":550")
                .contains("\"title\":\"Fight Club\"");
    }

    @Test
    void shouldReturnNotFoundWhenMovieDoesNotExist()
            throws Exception {

        stubTmdbJson(
                "/3/movie/999999999",
                404,
                "tmdb/movie-not-found-response.json"
        );

        HttpResponse<String> response =
                executeGet("/api/v1/movies/999999999");

        assertThat(response.statusCode())
                .isEqualTo(404);

        assertThat(response.body())
                .contains("\"code\":\"MOVIE_NOT_FOUND\"")
                .contains("\"status\":404")
                .contains("\"path\":\"/api/v1/movies/999999999\"");
    }

    @Test
    void shouldReturnRetryAfterWhenProviderIsRateLimited()
            throws Exception {

        tmdbWireMock.stubFor(
                get(urlPathEqualTo("/3/trending/movie/day"))
                        .withQueryParam(
                                "language",
                                equalTo(DEFAULT_LANGUAGE)
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

        HttpResponse<String> response =
                executeGet("/api/v1/movies/trending");

        assertThat(response.statusCode())
                .isEqualTo(503);

        assertThat(response.headers().firstValue(HttpHeaders.RETRY_AFTER))
                .contains("60");

        assertThat(response.body())
                .contains("\"code\":\"CATALOG_PROVIDER_RATE_LIMITED\"")
                .contains("\"message\":\"The catalog provider is temporarily rate limited\"")
                .contains("\"status\":503");
    }

    @Test
    void shouldRejectInvalidRequestParameters()
            throws Exception {

        HttpResponse<String> response =
                executeGet("/api/v1/movies/trending?window=MONTH");

        assertThat(response.statusCode())
                .isEqualTo(400);

        assertThat(response.body())
                .contains("\"code\":\"INVALID_PARAMETER\"")
                .contains("\"status\":400")
                .contains("MONTH")
                .contains("DAY, WEEK");
    }

    private void stubTmdbJson(
            String path,
            int status,
            String bodyFile
    ) {
        stubTmdbJson(
                path,
                DEFAULT_LANGUAGE,
                status,
                bodyFile
        );
    }

    private void stubTmdbJson(
            String path,
            String language,
            int status,
            String bodyFile
    ) {
        tmdbWireMock.stubFor(
                get(urlPathEqualTo(path))
                        .withQueryParam("language", equalTo(language))
                        .willReturn(
                                aResponse()
                                        .withStatus(status)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .withBodyFile(bodyFile)
                        )
        );
    }

    private HttpResponse<String> executeGet(String path)
            throws IOException, InterruptedException {

        return executeGet(path, null);
    }

    private HttpResponse<String> executeGet(
            String path,
            String acceptLanguage
    ) throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                );

        if (acceptLanguage != null) {
            requestBuilder.header(
                    HttpHeaders.ACCEPT_LANGUAGE,
                    acceptLanguage
            );
        }

        return httpClient.send(
                requestBuilder.GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
