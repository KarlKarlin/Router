package dev.Communication_Service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class VideoSessionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PARTICIPANT_PREFIX = "session:";

    public void createSession(String sessionId, String ownerUserId) {
        String key = PARTICIPANT_PREFIX + sessionId + ":participants";
        redisTemplate.opsForSet().add(key, ownerUserId);
    }

    public void joinSession(String sessionId, String userId) {
        String key = PARTICIPANT_PREFIX + sessionId + ":participants";
        redisTemplate.opsForSet().add(key, userId);
    }

    public void leaveSession(String sessionId, String userId) {
        String key = PARTICIPANT_PREFIX + sessionId + ":participants";
        redisTemplate.opsForSet().remove(key, userId);
    }

    public boolean isUserInSession(String sessionId, String userId) {
        String key = PARTICIPANT_PREFIX + sessionId + ":participants";
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId));
    }

    public Set<String> getParticipants(String sessionId) {
        String key = PARTICIPANT_PREFIX + sessionId + ":participants";
        return redisTemplate.opsForSet().members(key);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(PARTICIPANT_PREFIX + sessionId + ":participants");
    }

}

