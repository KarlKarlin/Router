package dev.Communication_Service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class InviteService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuthenticationInterface authenticationInterface;

    @Autowired
    private FileInterface fileInterface;

    private static final long INVITE_EXPIRATION = 600;

    public void sendInvite(String friendId, String token, UUID movieId) {

        if (!authenticationInterface.hasFriend(token, UUID.fromString(friendId))) {
            throw new IllegalArgumentException("Users are not friends.");
        }

        String sessionId = UUID.randomUUID().toString();

        String movieTitle = getMovieTitle(movieId);

        String key = "invite:" + sessionId + ":" + friendId;
        redisTemplate.opsForValue().set(key, movieTitle != null ? movieTitle : "unknown_movie", INVITE_EXPIRATION, TimeUnit.SECONDS);

        System.out.println("Invite sent to user " + friendId + " for movie: " + movieTitle);
    }

    public boolean isInvited(String sessionId, String friendId) {
        String key = "invite:" + sessionId + ":" + friendId;
        return redisTemplate.hasKey(key);
    }

    public String getMovieTitleFromInvite(String sessionId, String friendId) {
        String key = "invite:" + sessionId + ":" + friendId;
        return redisTemplate.opsForValue().get(key);
    }

    public void revokeInvite(String sessionId, String friendId) {
        redisTemplate.delete("invite:" + sessionId + ":" + friendId);
    }

    private String getMovieTitle(UUID movieId) {
        try {
            ResponseEntity<String> response = fileInterface.getMovieById(movieId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Failed to get movie title: " + e.getMessage());
        }
        return null;
    }

}
