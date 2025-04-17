package dev.Authentication.services;

import dev.Authentication.dto.FriendDTO;
import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class friendService {

    private final StringRedisTemplate redisTemplate;
    private final JWTService jwtService;
    private final userRepo userRepo;

    @Autowired
    public friendService(StringRedisTemplate redisTemplate, JWTService jwtService, userRepo userRepo) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    private String getEmailFromToken(String token) {
        token = token.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            return null;
        }
        return jwtService.extractUserName(token);
    }

    public ResponseEntity<String> sendInviteRequest(String targetEmail, String authorizationHeader) {
        String senderEmail = getEmailFromToken(authorizationHeader);
        if (senderEmail == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        if (senderEmail.equals(targetEmail)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't send request to yourself");

        if (isAlreadyInvited(senderEmail, targetEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You already invited this person");
        if (hasIncomingInvite(targetEmail, senderEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This person has already invited you");

        User friend = userRepo.findByEmail(targetEmail).orElse(null);
        if (friend == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if (hasFriend(senderEmail, targetEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This person is already your friend");

        redisTemplate.opsForSet().add(getOutgoingKey(senderEmail), targetEmail);
        redisTemplate.opsForSet().add(getIncomingKey(targetEmail), senderEmail);

        return ResponseEntity.ok("Invite sent successfully");
    }

    public ResponseEntity<String> acceptFriendRequest(String fromEmail, String authorizationHeader) {
        String toEmail = getEmailFromToken(authorizationHeader);
        if (toEmail == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        if (!hasIncomingInvite(toEmail, fromEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No invite from this user");

        redisTemplate.opsForSet().remove(getIncomingKey(toEmail), fromEmail);
        redisTemplate.opsForSet().remove(getOutgoingKey(fromEmail), toEmail);

        User sender = userRepo.findByEmail(fromEmail).orElse(null);
        User receiver = userRepo.findByEmail(toEmail).orElse(null);
        if (sender == null || receiver == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the users does not exist");

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        userRepo.save(sender);
        userRepo.save(receiver);

        return ResponseEntity.ok("Friend request accepted");
    }

    public ResponseEntity<String> rejectFriendRequest(String fromEmail, String authorizationHeader) {
        String toEmail = getEmailFromToken(authorizationHeader);
        if (toEmail == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        if (!hasIncomingInvite(toEmail, fromEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No invite from this user");

        redisTemplate.opsForSet().remove(getIncomingKey(toEmail), fromEmail);
        redisTemplate.opsForSet().remove(getOutgoingKey(fromEmail), toEmail);

        return ResponseEntity.ok("Friend request rejected");
    }

    public ResponseEntity<Set<String>> getOutgoingRequests(String authorizationHeader) {
        String email = getEmailFromToken(authorizationHeader);
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Set<String> outgoing = redisTemplate.opsForSet().members(getOutgoingKey(email));
        return ResponseEntity.ok(outgoing);
    }

    public ResponseEntity<Set<String>> getIncomingRequests(String authorizationHeader) {
        String email = getEmailFromToken(authorizationHeader);
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Set<String> incoming = redisTemplate.opsForSet().members(getIncomingKey(email));
        return ResponseEntity.ok(incoming);
    }

    public ResponseEntity<String> cancelInviteRequest(String targetEmail, String authorizationHeader) {
        String senderEmail = getEmailFromToken(authorizationHeader);
        if (senderEmail == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        if (!isAlreadyInvited(senderEmail, targetEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No outgoing invite to this person");

        redisTemplate.opsForSet().remove(getOutgoingKey(senderEmail), targetEmail);
        redisTemplate.opsForSet().remove(getIncomingKey(targetEmail), senderEmail);

        return ResponseEntity.ok("Invite canceled successfully");
    }

    public ResponseEntity<?> getAllFriends(String authorizationHeader) {
        String email = getEmailFromToken(authorizationHeader);
        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        Set<FriendDTO> friends = user.getFriends().stream()
                .map(f -> new FriendDTO(f.getId(), f.getEmail(), f.getUsername()))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(friends);
    }

    public ResponseEntity<String> deleteFriend(String friendEmail, String authorizationHeader) {
        String userEmail = getEmailFromToken(authorizationHeader);
        if (userEmail == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");

        if (userEmail.equals(friendEmail)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't unfriend yourself");

        User user = userRepo.findByEmail(userEmail).orElse(null);
        User friend = userRepo.findByEmail(friendEmail).orElse(null);
        if (user == null || friend == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the users does not exist");

        boolean removedFromUser = user.getFriends().remove(friend);
        boolean removedFromFriend = friend.getFriends().remove(user);

        if (!removedFromUser && !removedFromFriend) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are not friends with this person");

        userRepo.save(user);
        userRepo.save(friend);

        return ResponseEntity.ok("Friend successfully removed");
    }

    public void deleteAllFriendDataForUser(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            for (User friend : user.getFriends()) {
                friend.getFriends().remove(user);
                userRepo.save(friend);
            }
            user.getFriends().clear();
            userRepo.save(user);
        }

        // Delete Redis friend requests
        deleteRedisRequestsForUser(email);
    }

    private void deleteRedisRequestsForUser(String email) {
        Set<String> outgoing = redisTemplate.opsForSet().members(getOutgoingKey(email));
        if (outgoing != null) {
            outgoing.forEach(target -> redisTemplate.opsForSet().remove(getIncomingKey(target), email));
        }

        Set<String> incoming = redisTemplate.opsForSet().members(getIncomingKey(email));
        if (incoming != null) {
            incoming.forEach(sender -> redisTemplate.opsForSet().remove(getOutgoingKey(sender), email));
        }

        redisTemplate.delete(getOutgoingKey(email));
        redisTemplate.delete(getIncomingKey(email));
    }

    public boolean hasFriend(String token, String friendEmail) {
        String email = getEmailFromToken(token);
        if (email == null) return false;

        Optional<User> friendUser = userRepo.findByEmail(friendEmail);
        return friendUser.map(f -> f.getFriends().contains(f)).orElse(false);
    }

    private boolean isAlreadyInvited(String from, String to) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(getOutgoingKey(from), to));
    }

    private boolean hasIncomingInvite(String target, String sender) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(getIncomingKey(target), sender));
    }

    private String getOutgoingKey(String email) {
        return "outgoing:" + email;
    }

    private String getIncomingKey(String email) {
        return "incoming:" + email;
    }

    private String getFriendsKey(String email) {
        return "friends:" + email;
    }
}
