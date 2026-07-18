package dev.alberto.moviecatalog.web.error;

import dev.alberto.moviecatalog.application.exception.MovieProviderAccessException;
import dev.alberto.moviecatalog.application.exception.MovieProviderRateLimitedException;
import dev.alberto.moviecatalog.application.exception.MovieProviderRequestException;
import dev.alberto.moviecatalog.application.exception.MovieProviderUnavailableException;
import dev.alberto.moviecatalog.domain.exception.MovieNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMovieNotFound(
            MovieNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "MOVIE_NOT_FOUND",
                "The requested movie could not be found",
                request
        );
    }

    @ExceptionHandler(MovieProviderRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderRequest(
            MovieProviderRequestException exception,
            HttpServletRequest request
    ) {
        log.error(
                "The movie provider rejected the request",
                exception
        );

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "MOVIE_PROVIDER_REQUEST_ERROR",
                "The movie provider rejected the request",
                request
        );
    }

    @ExceptionHandler(MovieProviderAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderAccess(
            MovieProviderAccessException exception,
            HttpServletRequest request
    ) {
        log.error(
                "The movie provider rejected the service credentials",
                exception
        );

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "MOVIE_PROVIDER_ACCESS_ERROR",
                "The movie provider rejected the service credentials",
                request
        );
    }

    @ExceptionHandler(MovieProviderRateLimitedException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderRateLimit(
            MovieProviderRateLimitedException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "The movie provider rate limit was exceeded. Retry after: {}",
                exception.getRetryAfterSeconds()
        );

        ResponseEntity.BodyBuilder response =
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE);

        if (exception.getRetryAfterSeconds() != null) {
            response.header(
                    HttpHeaders.RETRY_AFTER,
                    exception.getRetryAfterSeconds().toString()
            );
        }

        return response.body(
                createError(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "MOVIE_PROVIDER_RATE_LIMITED",
                        "The movie provider is temporarily rate limited",
                        request
                )
        );
    }

    @ExceptionHandler(MovieProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderUnavailable(
            MovieProviderUnavailableException exception,
            HttpServletRequest request
    ) {
        log.error(
                "The movie provider is unavailable",
                exception
        );

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "MOVIE_PROVIDER_UNAVAILABLE",
                "The movie provider is currently unavailable",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                buildTypeMismatchMessage(exception),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String message = "Required parameter '%s' is missing"
                .formatted(exception.getParameterName());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                message,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleRequestBodyValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> "%s: %s".formatted(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .findFirst()
                .orElse("Request validation failed");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message,
                request
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        String message = exception
                .getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Request parameter validation failed");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST_BODY",
                "The request body is missing or malformed",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        String message = buildMethodNotSupportedMessage(exception);

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(exception.getHeaders())
                .body(
                        createError(
                                HttpStatus.METHOD_NOT_ALLOWED,
                                "METHOD_NOT_ALLOWED",
                                message,
                                request
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error processing request {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred",
                request
        );
    }

    private String buildTypeMismatchMessage(
            MethodArgumentTypeMismatchException exception
    ) {
        String parameterName = exception.getName();
        String rejectedValue = String.valueOf(exception.getValue());
        Class<?> requiredType = exception.getRequiredType();

        if (requiredType != null && requiredType.isEnum()) {
            String allowedValues = Arrays
                    .stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return """
                    Invalid value '%s' for parameter '%s'. Allowed values: %s
                    """
                    .formatted(
                            rejectedValue,
                            parameterName,
                            allowedValues
                    )
                    .strip();
        }

        return "Invalid value '%s' for parameter '%s'"
                .formatted(
                        rejectedValue,
                        parameterName
                );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(
                        createError(
                                status,
                                code,
                                message,
                                request
                        )
                );
    }

    private ApiErrorResponse createError(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return new ApiErrorResponse(
                code,
                message,
                status.value(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    private String buildMethodNotSupportedMessage(
            HttpRequestMethodNotSupportedException exception
    ) {
        String[] supportedMethods = exception.getSupportedMethods();

        if (supportedMethods == null || supportedMethods.length == 0) {
            return "HTTP method '%s' is not supported for this endpoint"
                    .formatted(exception.getMethod());
        }

        return "HTTP method '%s' is not supported for this endpoint. Allowed methods: %s"
                .formatted(
                        exception.getMethod(),
                        String.join(", ", supportedMethods)
                );
    }
}