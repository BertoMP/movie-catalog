package dev.alberto.moviecatalog.web.controller;

import dev.alberto.moviecatalog.application.exception
        .MovieProviderAccessException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderRateLimitedException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderRequestException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderUnavailableException;
import dev.alberto.moviecatalog.domain.exception.MovieNotFoundException;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.testdata.domain
        .MovieDetailMother;
import dev.alberto.moviecatalog.testdata.domain
        .MovieSummaryMother;
import dev.alberto.moviecatalog.web.mapper.MovieApiMapper;
import dev.alberto.moviecatalog.web.response
        .MovieDetailResponse;
import dev.alberto.moviecatalog.web.response
        .MovieSummaryResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito
        .MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(MovieApiController.class)
class MovieApiControllerTest {

    private static final String BASE_PATH =
            "/api/v1/movies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieCatalogService movieCatalogService;

    @MockitoBean
    private MovieApiMapper movieApiMapper;

    @TestConfiguration
    static class CacheTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Test
    void shouldReturnDailyTrendingMoviesByDefault()
            throws Exception {

        List<MovieSummary> domainMovies =
                MovieSummaryMother.randomList(2);

        List<MovieSummaryResponse> response =
                List.of(
                        Instancio.create(
                                MovieSummaryResponse.class
                        ),
                        Instancio.create(
                                MovieSummaryResponse.class
                        )
                );

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY
        )).thenReturn(domainMovies);

        when(movieApiMapper.toResponse(domainMovies))
                .thenReturn(response);

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(
                                        response
                                )
                        )
                );

        verify(movieCatalogService)
                .getTrendingMovies(TrendingWindow.DAY);

        verify(movieApiMapper)
                .toResponse(domainMovies);
    }

    @Test
    void shouldReturnWeeklyTrendingMovies()
            throws Exception {

        List<MovieSummary> domainMovies =
                MovieSummaryMother.randomList(2);

        List<MovieSummaryResponse> response =
                List.of(
                        Instancio.create(
                                MovieSummaryResponse.class
                        )
                );

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.WEEK
        )).thenReturn(domainMovies);

        when(movieApiMapper.toResponse(domainMovies))
                .thenReturn(response);

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                                .queryParam("window", "WEEK")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(
                                        response
                                )
                        )
                );

        verify(movieCatalogService)
                .getTrendingMovies(TrendingWindow.WEEK);
    }

    @Test
    void shouldRejectInvalidTrendingWindow()
            throws Exception {

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                                .queryParam(
                                        "window",
                                        "DAYssss"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        containsString(
                                                "DAYssss"
                                        )
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        containsString(
                                                "DAY, WEEK"
                                        )
                                )
                );

        verifyNoInteractions(
                movieCatalogService,
                movieApiMapper
        );
    }

    @Test
    void shouldRejectUnsupportedHttpMethod()
            throws Exception {

        mockMvc.perform(
                        post(BASE_PATH + "/trending")
                                .queryParam("window", "DAY")
                )
                .andExpect(
                        status().isMethodNotAllowed()
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ALLOW,
                                containsString("GET")
                        )
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("METHOD_NOT_ALLOWED")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(405)
                );
    }

    @Test
    void shouldReturnMovieDetail()
            throws Exception {

        Long movieId = 550L;

        MovieDetail domainMovie =
                MovieDetailMother.withId(movieId);

        MovieDetailResponse response =
                Instancio.create(
                        MovieDetailResponse.class
                );

        when(movieCatalogService.getMovieDetail(movieId))
                .thenReturn(domainMovie);

        when(movieApiMapper.toResponse(domainMovie))
                .thenReturn(response);

        mockMvc.perform(
                        get(BASE_PATH + "/{movieId}", movieId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(
                                        response
                                )
                        )
                );

        verify(movieCatalogService)
                .getMovieDetail(movieId);

        verify(movieApiMapper)
                .toResponse(domainMovie);
    }

    @Test
    void shouldReturnNotFoundWhenMovieDoesNotExist()
            throws Exception {

        Long movieId = 999_999_999L;

        when(movieCatalogService.getMovieDetail(movieId))
                .thenThrow(
                        new MovieNotFoundException(movieId)
                );

        mockMvc.perform(
                        get(BASE_PATH + "/{movieId}", movieId)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("MOVIE_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        BASE_PATH + "/" + movieId
                                )
                );
    }

    @Test
    void shouldRejectNegativeMovieId()
            throws Exception {

        mockMvc.perform(
                        get(BASE_PATH + "/{movieId}", -1)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                );

        verifyNoInteractions(
                movieCatalogService,
                movieApiMapper
        );
    }

    @Test
    void shouldRejectNonNumericMovieId()
            throws Exception {

        mockMvc.perform(
                        get(BASE_PATH + "/{movieId}", "text")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                );

        verifyNoInteractions(
                movieCatalogService,
                movieApiMapper
        );
    }

    @Test
    void shouldReturnBadGatewayWhenProviderRejectsAccess()
            throws Exception {

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY
        )).thenThrow(
                new MovieProviderAccessException(
                        new RuntimeException(
                                "Invalid provider token"
                        )
                )
        );

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                )
                .andExpect(status().isBadGateway())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "MOVIE_PROVIDER_ACCESS_ERROR"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(502)
                );
    }

    @Test
    void shouldReturnBadGatewayWhenProviderRejectsRequest()
            throws Exception {

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY
        )).thenThrow(
                new MovieProviderRequestException(
                        new RuntimeException(
                                "Invalid provider request"
                        )
                )
        );

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                )
                .andExpect(status().isBadGateway())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "MOVIE_PROVIDER_REQUEST_ERROR"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(502)
                );
    }

    @Test
    void shouldReturnServiceUnavailableWhenProviderFails()
            throws Exception {

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY
        )).thenThrow(
                new MovieProviderUnavailableException(
                        new RuntimeException(
                                "Provider unavailable"
                        )
                )
        );

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "MOVIE_PROVIDER_UNAVAILABLE"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(503)
                );
    }

    @Test
    void shouldReturnRetryAfterWhenProviderIsRateLimited()
            throws Exception {

        when(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY
        )).thenThrow(
                new MovieProviderRateLimitedException(
                        60L,
                        new RuntimeException(
                                "Rate limit exceeded"
                        )
                )
        );

        mockMvc.perform(
                        get(BASE_PATH + "/trending")
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        header().string(
                                HttpHeaders.RETRY_AFTER,
                                "60"
                        )
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "MOVIE_PROVIDER_RATE_LIMITED"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(503)
                );
    }
}
