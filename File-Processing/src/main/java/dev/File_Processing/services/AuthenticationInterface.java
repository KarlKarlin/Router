package dev.File_Processing.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("AUTHENTICATION")
public interface AuthenticationInterface {

    @GetMapping("/auth/extract-email")
    public ResponseEntity<String> extractEmail(@RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/auth/setLikedMovie")
    public ResponseEntity<String> setLikedMovie(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam String title);

    @PutMapping("/auth/addRatedMovie")
    public ResponseEntity<String> addRatedMovie(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String title,
            @RequestParam float rating);

    @GetMapping("/auth/hasRole")
    public ResponseEntity<Boolean> hasRole(@RequestHeader(value = "Authorization", required = false) String authorizationHeader);

    @GetMapping("/auth/getMostLiked")
    public ResponseEntity<String> getMostLiked(@RequestHeader(value = "Authorization", required = false) String authorizationHeader);

}
