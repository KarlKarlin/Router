package dev.File_Processing.controllers;

import dev.File_Processing.models.File;
import dev.File_Processing.services.RecommendationSyncService;
import dev.File_Processing.services.searchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies/search")
public class searchController {

    @Autowired
    private RecommendationSyncService recommendationService;

    @Autowired
    private searchHistoryService searchHistoryService;

    @GetMapping("/search")
    public ResponseEntity<List<File>> searchResponse(@RequestParam("keyword") String keyword, @RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        searchHistoryService.saveHistory(authorizationHeader, keyword);

        return ResponseEntity.ok().body(recommendationService.searchMovies(keyword));

    }

    @GetMapping("/getHistory")
    public List<Map<String, String>> getHistory(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        return searchHistoryService.getHistory(authorizationHeader);

    }

    @DeleteMapping("/clearHistory")
    public ResponseEntity<String> clearHistory(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        return searchHistoryService.clearHistory(authorizationHeader);

    }

}
