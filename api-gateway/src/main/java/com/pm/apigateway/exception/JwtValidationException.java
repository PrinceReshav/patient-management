package com.pm.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice // Tells spring that this class will handle an exception
// Instead of sending 500 internal server error to Frontend client in case of wrong bearer token we send 401 Unauthorized
public class JwtValidationException {
    @ExceptionHandler(WebClientResponseException.class)
    // Mono object is used by "FILTER CHAIN" to tell spring that current filter is finished
    public Mono<Void> handleUnauthorizedException(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
    }
}
