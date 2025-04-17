package dev.Communication_Service.controllers;

import dev.Communication_Service.services.InviteService;
import dev.Communication_Service.services.VideoSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/meeting")
public class VideoCallController {

    @Autowired
    private VideoSessionService sessionService;

    @Autowired
    private InviteService inviteService;

    @PostMapping("/create")
    public ResponseEntity<String> createSession(@RequestParam String sessionId,
                                                @RequestParam String ownerId) {
        sessionService.createSession(sessionId, ownerId);
        return ResponseEntity.ok("Session created.");
    }

    @PostMapping("/invite")
    public ResponseEntity<String> invite(@RequestParam String friendId,
                                         @RequestParam UUID movieId,
                                         @RequestHeader("Authorization") String token) {

        inviteService.sendInvite(friendId, token, movieId);

        return ResponseEntity.ok("Invite sent.");

    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestParam String sessionId,
                                       @RequestParam String userId) {
        if (!inviteService.isInvited(sessionId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not invited.");
        }
        sessionService.joinSession(sessionId, userId);
        return ResponseEntity.ok("Joined.");

    }

    @PostMapping("/leave")
    public ResponseEntity<String> leave(@RequestParam String sessionId,
                                        @RequestParam String userId) {
        sessionService.leaveSession(sessionId, userId);
        return ResponseEntity.ok("Left session.");
    }

    @GetMapping("/participants")
    public ResponseEntity<Set<String>> getParticipants(@RequestParam String sessionId) {
        return ResponseEntity.ok(sessionService.getParticipants(sessionId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMeeting(@RequestParam String sessionId){
        sessionService.deleteSession(sessionId);
        return ResponseEntity.ok("Session was successfully deleted");
    }

}