package dev.Communication_Service.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@FeignClient("FILE-PROCESSING")
public interface FileInterface {

    @GetMapping("/api/files/getMovieById")
    public ResponseEntity<String> getMovieById(UUID movieId);

}
