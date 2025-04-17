package dev.Features_Service.services;

import dev.Features_Service.dto.FileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "FILE-PROCESSING")
public interface FileInterface {

    @GetMapping("/api/movies/recommendByGenre")
    public ResponseEntity<List<FileDTO>> recommendByGenre(@RequestParam String genre);

}
