package dev.alberto.moviecatalog.testdata.feign;

import feign.Request;
import feign.Response;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class FeignResponseMother {

    private FeignResponseMother() {
    }

    public static Response withStatusAndBody(
            int status,
            String body
    ) {
        return withHeaders(
                status,
                body,
                Map.of()
        );
    }

    public static Response withHeaders(
            int status,
            String body,
            Map<String, Collection<String>> headers
    ) {
        Response.Builder builder = Response.builder()
                .status(status)
                .reason("Test response")
                .headers(headers)
                .request(createRequest());

        if (body != null) {
            builder.body(
                    body,
                    StandardCharsets.UTF_8
            );
        }

        return builder.build();
    }

    public static Response withRetryAfter(
            int status,
            String body,
            long retryAfterSeconds
    ) {
        return withHeaders(
                status,
                body,
                Map.of(
                        "Retry-After",
                        List.of(
                                String.valueOf(retryAfterSeconds)
                        )
                )
        );
    }

    public static Response withoutBody(int status) {
        return withStatusAndBody(
                status,
                null
        );
    }

    public static Response withMalformedBody(int status) {
        return withStatusAndBody(
                status,
                "this-is-not-valid-json"
        );
    }

    private static Request createRequest() {
        return Request.create(
                Request.HttpMethod.GET,
                "/test",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );
    }
}