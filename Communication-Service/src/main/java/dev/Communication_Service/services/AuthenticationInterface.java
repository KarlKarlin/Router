package dev.Communication_Service.services;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient("AUTHENTICATION")
public interface AuthenticationInterface {

    @GetMapping("/auth/extract-email")
    public ResponseEntity<String> extractEmail(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/auth/hasFriend")
    public boolean hasFriend(@RequestHeader("Authorization") String token, @RequestParam UUID friendId);

}