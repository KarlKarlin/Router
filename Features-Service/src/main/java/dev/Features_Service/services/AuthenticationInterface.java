package dev.Features_Service.services;

import dev.Features_Service.dto.FileDTO;
import dev.Features_Service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient("AUTHENTICATION")
public interface AuthenticationInterface {

    @GetMapping("/auth/user")
    public ResponseEntity<UserDTO> getUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader);

    @PostMapping("/auth/add-coin")
    public ResponseEntity<String> addCoin(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int amount);

    @GetMapping("/auth/getRatedMovies")
    public ResponseEntity<List<FileDTO>> getRatedMovies(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/auth/extract-email")
    public ResponseEntity<String> extractEmail(@RequestHeader("Authorization") String authorizationHeader);

}
