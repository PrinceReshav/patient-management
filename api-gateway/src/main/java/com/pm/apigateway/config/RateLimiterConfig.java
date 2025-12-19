package com.pm.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean // This configuration tells rate limiter what to check ?
    public KeyResolver ipKeyResolver() // Specifying how we want spring cloud gateway's rate limiter to determine
                                      // how to uniquely identify a client , here IP Address
                                     // ipKeyResolver should match with name in application.yml
    {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    // EX : -> if IP = 123.121.3.3 then this is the key that gets stored in Redis and it will alco store
    // COUNT , i.e, how many requests this IP address has made for any given second
    // This is stored in Redis and used by Spring Cloud Gateway to determine how to Rate limiter a request

}
