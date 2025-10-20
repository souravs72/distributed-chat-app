package com.chatapp.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service routes
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("auth-circuit-breaker")))
                        .uri("lb://auth-service"))
                
                // User Service routes
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("user-circuit-breaker")))
                        .uri("lb://user-service"))
                
                // Chat Service routes
                .route("chat-service", r -> r.path("/api/chat/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("chat-circuit-breaker")))
                        .uri("lb://chat-service"))
                
                // Presence Service routes
                .route("presence-service", r -> r.path("/api/presence/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("presence-circuit-breaker")))
                        .uri("lb://presence-service"))
                
                // Media Service routes
                .route("media-service", r -> r.path("/api/media/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("media-circuit-breaker")))
                        .uri("lb://media-service"))
                
                // Notification Service routes
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config.setName("notification-circuit-breaker")))
                        .uri("lb://notification-service"))
                
                // WebSocket routes
                .route("websocket-service", r -> r.path("/ws/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://websocket-service"))
                
                .build();
    }
}
