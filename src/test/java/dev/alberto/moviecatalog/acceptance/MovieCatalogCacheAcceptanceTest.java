package dev.alberto.moviecatalog.acceptance;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cache.type=redis",
                "tmdb.base-url=${wiremock.tmdb.base-url}/3"
        }
)
@ActiveProfiles("test-cache")
@EnableWireMock(
        @ConfigureWireMock(
                name = "tmdb",
                baseUrlProperties = "wiremock.tmdb.base-url"
        )
)
class MovieCatalogCacheAcceptanceTest {

    private static final String DEFAULT_LANGUAGE = "en-US";
    private static final String REDIS_USERNAME = "movie_catalog";
    private static final String REDIS_PASSWORD = "test-password";
    private static final String TRENDING_CACHE_KEY =
            "catalog::trending-movies::day::en-US";
    private static final String MOVIE_DETAIL_CACHE_KEY =
            "catalog::movie-detail::550::en-US";

    @Container
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:8-alpine")
                    .withExposedPorts(6379)
                    .withCommand(
                            "/bin/sh",
                            "-c",
                            ("printf 'user default off\\nuser %s on >%s "
                                    + "~catalog::* &* +@all\\n' > /tmp/users.acl "
                                    + "&& exec redis-server --aclfile /tmp/users.acl")
                                    .formatted(
                                            REDIS_USERNAME,
                                            REDIS_PASSWORD
                                    )
                    );

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @InjectWireMock("tmdb")
    private WireMockServer tmdbWireMock;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> REDIS.getMappedPort(6379)
        );
        registry.add("spring.data.redis.username", () -> REDIS_USERNAME);
        registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        tmdbWireMock.resetAll();

        Set<String> keys = redisTemplate.keys("catalog::*");

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldServeTrendingMoviesFromRedisAfterFirstRequest()
            throws Exception {

        stubTmdbJson(
                "/3/trending/movie/day",
                "tmdb/trending-movies-response.json"
        );

        HttpResponse<String> firstResponse =
                executeGet("/api/v1/movies/trending");
        HttpResponse<String> cachedResponse =
                executeGet("/api/v1/movies/trending");

        assertThat(firstResponse.statusCode()).isEqualTo(200);
        assertThat(cachedResponse.statusCode()).isEqualTo(200);
        assertThat(cachedResponse.body()).isEqualTo(firstResponse.body());
        assertThat(redisTemplate.hasKey(TRENDING_CACHE_KEY)).isTrue();

        tmdbWireMock.verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/3/trending/movie/day"))
                        .withQueryParam(
                                "language",
                                equalTo(DEFAULT_LANGUAGE)
                        )
        );
    }

    @Test
    void shouldServeMovieDetailFromRedisAfterFirstRequest()
            throws Exception {

        stubTmdbJson(
                "/3/movie/550",
                "tmdb/movie-detail-response.json"
        );

        HttpResponse<String> firstResponse =
                executeGet("/api/v1/movies/550");
        HttpResponse<String> cachedResponse =
                executeGet("/api/v1/movies/550");

        assertThat(firstResponse.statusCode()).isEqualTo(200);
        assertThat(cachedResponse.statusCode()).isEqualTo(200);
        assertThat(cachedResponse.body()).isEqualTo(firstResponse.body());
        assertThat(redisTemplate.hasKey(MOVIE_DETAIL_CACHE_KEY)).isTrue();

        tmdbWireMock.verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/3/movie/550"))
                        .withQueryParam(
                                "language",
                                equalTo(DEFAULT_LANGUAGE)
                        )
        );
    }

    private void stubTmdbJson(
            String path,
            String bodyFile
    ) {
        tmdbWireMock.stubFor(
                get(urlPathEqualTo(path))
                        .withQueryParam(
                                "language",
                                equalTo(DEFAULT_LANGUAGE)
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .GET()
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
