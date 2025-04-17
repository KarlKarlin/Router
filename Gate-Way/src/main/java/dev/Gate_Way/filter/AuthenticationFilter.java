package dev.Gate_Way.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<Object> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private AuthService authService;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {

                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new UnauthorizedAccessException("Missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                System.out.println(exchange.getRequest().getPath());

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new UnauthorizedAccessException("Missing or invalid authorization header");
                }

                String token = authHeader.replace("Bearer ", "");

                return authService.validateToken(token)
                        .flatMap(isValid -> {

                            if (!Boolean.TRUE.equals(isValid)) {
                                throw new UnauthorizedAccessException("Invalid token");
                            }

                            return chain.filter(exchange);
                        })
                        .onErrorResume(e -> {
                            System.out.println("Error during token validation: " + e.getMessage());
                            throw new UnauthorizedAccessException("Invalid token", e);
                        });
            }


            return chain.filter(exchange);
        };
    }

    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }

        public UnauthorizedAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}