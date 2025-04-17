package dev.Authentication.services;

import dev.Authentication.models.Role;
import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
public class userService {

    @Autowired
    private userRepo userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User register(User user) {

        Optional<User> foundUser = userRepo.findByEmail(user.getEmail());

        if (foundUser.isPresent()) {
            throw new IllegalArgumentException();
        }

        if(user.getPassword().length() < 8){
            throw new IllegalArgumentException("User's password is too short!");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRoles(Set.of(Role.ROLE_ADMIN));
        user.setCoins(0);
        user.setTier("\uD83D\uDD30 Beginner");
        userRepo.save(user);
        return user;
    }

    public String verify(User user) {
        Optional<User> foundUser = userRepo.findByEmail(user.getEmail());

        if (foundUser.isEmpty()) {
            return "fail";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(user.getEmail());
            }
        } catch (Exception e) {
            return "fail";
        }

        return "fail";
    }


    public ResponseEntity<String> delete(String email) {
        Optional<User> user = userRepo.findByEmail(email);

        if (user.isPresent()) {

            try {

                userRepo.delete(user.get());

                return ResponseEntity.ok("User deleted successfully");

            } catch (Exception e) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
            }
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        }
    }
}