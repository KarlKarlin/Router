package dev.File_Processing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class searchHistoryService {

    @Autowired
    private AuthenticationInterface authenticationInterface;

    private final StringRedisTemplate redisTemplate;

    public searchHistoryService(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public ResponseEntity<String> saveHistory(String authorizationHeader, String keyword) {
        String userEmail = authenticationInterface.extractEmail(authorizationHeader).getBody();

        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Sign in to continue!");
        }

        String redisKey = "searchHistory:" + userEmail;

        List<String> currentHistory = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (currentHistory != null && currentHistory.contains(keyword)) {
            return ResponseEntity.ok("Proceed");
        }

        redisTemplate.opsForList().leftPush(redisKey, keyword);

        redisTemplate.opsForList().trim(redisKey, 0, 49);

        redisTemplate.expire(redisKey, Duration.ofDays(10));

        return ResponseEntity.ok("History was saved");
    }


    public List<Map<String, String>> getHistory(String authorizationHeader) {
        String userEmail = authenticationInterface.extractEmail(authorizationHeader).getBody();

        if (userEmail == null) return Collections.emptyList();

        String redisKey = "searchHistory:" + userEmail;

        List<String> historyList = redisTemplate.opsForList().range(redisKey, 0, -1);
        List<Map<String, String>> result = new ArrayList<>();

        if (historyList != null) {
            for (String keyword : historyList) {
                Map<String, String> entry = new HashMap<>();
                entry.put("historyValue", keyword);
                result.add(entry);
            }
        }

        return result;
    }

    public ResponseEntity<String> clearHistory(String authorizationHeader) {
        String userEmail = authenticationInterface.extractEmail(authorizationHeader).getBody();

        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Sign in to continue!");
        }

        String redisKey = "searchHistory:" + userEmail;

        redisTemplate.delete(redisKey);

        return ResponseEntity.ok("History was successfully cleared");
    }
}

