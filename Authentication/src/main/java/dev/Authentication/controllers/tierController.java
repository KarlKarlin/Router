package dev.Authentication.controllers;

import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import dev.Authentication.services.JWTService;
import dev.Authentication.services.tierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user/tiers")
public class tierController {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private userRepo userRepo;

    @Autowired
    private tierService tierService;

    @PostMapping("/assignTier")
    public ResponseEntity<String> assignTier(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        try {

            String token = authorizationHeader.replace("Bearer ", "");

            if (token.isEmpty()) {
                return ResponseEntity.status(404).body("Unauthorized");
            }

            String email = jwtService.extractUserName(token);

            Optional<User> user = userRepo.findByEmail(email);

            if (user.isEmpty()) return null;

            tierService.assignTier(user.get());

            return ResponseEntity.ok("Tier was successfully set");

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have small amount of coins to assign new tier");
        }

    }

}
