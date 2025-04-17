package dev.Authentication.controllers;

import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import dev.Authentication.services.friendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class friendController {

    @Autowired
    private friendService friendService;

    @Autowired
    private userController userController;

    @Autowired
    private userRepo userRepo;

    @PostMapping("/invite")
    public ResponseEntity<String> sendInvite(@RequestParam String email,
                                             @RequestHeader("Authorization") String token) {

        try {
            return friendService.sendInviteRequest(email, token);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Intentional server error");
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvite(@RequestParam String fromEmail,
                                               @RequestHeader("Authorization") String token) {
        return friendService.acceptFriendRequest(fromEmail, token);
    }

    @PostMapping("/reject")
    public ResponseEntity<String> rejectInvite(@RequestParam String fromEmail,
                                               @RequestHeader("Authorization") String token) {
        return friendService.rejectFriendRequest(fromEmail, token);
    }

    @GetMapping("/incoming")
    public ResponseEntity<Set<String>> getIncomingRequests(@RequestHeader("Authorization") String token) {
        return friendService.getIncomingRequests(token);
    }

    @GetMapping("/outgoing")
    public ResponseEntity<Set<String>> getOutgoingRequests(@RequestHeader("Authorization") String token) {
        return friendService.getOutgoingRequests(token);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelInvite(@RequestParam String email,
                                               @RequestHeader("Authorization") String token) {
        return friendService.cancelInviteRequest(email, token);
    }

    @GetMapping("/getAllFriends")
    public ResponseEntity<?> getAllFriends(@RequestHeader("Authorization") String token){
        return friendService.getAllFriends(token);
    }

    @GetMapping("/hasFriend")
    public boolean hasFriend(@RequestHeader("Authorization") String token, @RequestParam UUID friendId) {

        ResponseEntity<Optional<User>> user = userController.extractUser(token);

        Optional<User> friendUser = userRepo.findById(friendId);

        if (user.getBody().isEmpty()) return false;

        return friendUser.filter(value -> user.getBody().get().getFriends().contains(value)).isPresent();

    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFriend(@RequestParam String friendEmail, @RequestHeader("Authorization") String token){
        return friendService.deleteFriend(friendEmail, token);
    }

}
