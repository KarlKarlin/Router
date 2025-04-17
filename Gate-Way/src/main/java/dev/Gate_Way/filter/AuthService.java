package dev.Gate_Way.filter;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final WebClient webClient;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
                .uri("/auth/validateToken")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnTerminate(() -> System.out.println("Validation complete")) // Log after the validation completes
                .doOnError(e -> System.out.println("Error occurred: " + e.getMessage())) // Log errors
                .onErrorResume(e -> Mono.just(false));
    }
}