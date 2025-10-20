package com.chatapp.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientId = getClientId(request);
            
            Bucket bucket = cache.computeIfAbsent(clientId, this::createNewBucket);
            
            if (bucket.tryConsume(1)) {
                return chain.filter(exchange);
            } else {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().add("X-RateLimit-Remaining", "0");
                return response.setComplete();
            }
        };
    }

    private Bucket createNewBucket(String clientId) {
        Bandwidth limit = Bandwidth.classic(config.getCapacity(), 
                Refill.intervally(config.getRefillTokens(), Duration.ofMinutes(config.getRefillDuration())));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientId(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    public static class Config {
        private int capacity = 100;
        private int refillTokens = 100;
        private int refillDuration = 1;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public void setRefillTokens(int refillTokens) {
            this.refillTokens = refillTokens;
        }

        public int getRefillDuration() {
            return refillDuration;
        }

        public void setRefillDuration(int refillDuration) {
            this.refillDuration = refillDuration;
        }
    }
}
