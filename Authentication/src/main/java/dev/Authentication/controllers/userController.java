package dev.Authentication.controllers;

import dev.Authentication.dto.FileDTO;
import dev.Authentication.dto.UserDTO;
import dev.Authentication.models.Role;
import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import dev.Authentication.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class userController {

    @Autowired
    private userService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private userRepo userRepo;

    @Autowired
    private friendService friendService;

    @Autowired
    private FeaturesInterface featuresInterface;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @PostMapping("/register")
    public User register(@RequestBody User user){

    return userService.register(user);

    }

    @PostMapping("/login")
    public String login (@RequestBody User user){
        return userService.verify(user);
    }

    @GetMapping("/extract-email")
    public ResponseEntity<String> extractEmail(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");
        }

        String email = jwtService.extractUserName(token);
        return ResponseEntity.ok(email);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String authorizationHeader) {

        String email = extractEmail(authorizationHeader).getBody();

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authorization token");
        }

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        for (User friend : user.getFriends()) {
            friend.getFriends().remove(user);
            userRepo.save(friend);
        }

        user.getFriends().clear();
        userRepo.save(user);

        userRepo.delete(user);

        friendService.deleteAllFriendDataForUser(email);

        featuresInterface.deleteMap(authorizationHeader);

        return ResponseEntity.ok("User and friendships successfully deleted");
    }


    @GetMapping("/extract-user")
    public ResponseEntity<Optional<User>> extractUser(@RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");

        if(token.isEmpty()){
            return ResponseEntity.status(404).body(Optional.empty());
        }

        String email = jwtService.extractUserName(token);

        Optional<User> user = userRepo.findByEmail(email);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtService.extractUserName(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }

    @PutMapping("/setLikedMovie")
    public ResponseEntity<String> setLikedMovie(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam String title) {

        ResponseEntity<Optional<User>> userResponse = extractUser(authorizationHeader);

        Optional<User> optionalUser = userResponse.getBody();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or unauthorized");
        }

        User user = optionalUser.get();

        if (user.getLikedMovies() == null || user.getLikedMovies().length == 0) {
            user.setLikedMovies(new String[]{title});
        } else {
            if (!Arrays.asList(user.getLikedMovies()).contains(title)) {
                String[] updatedLikedMovies = Arrays.copyOf(user.getLikedMovies(), user.getLikedMovies().length + 1);
                updatedLikedMovies[user.getLikedMovies().length] = title;
                user.setLikedMovies(updatedLikedMovies);
            }
        }

        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("Movie liked successfully");
    }

    @PutMapping("/addRatedMovie")
    public ResponseEntity<String> addRatedMovie(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String title,
            @RequestParam float rating) {

        ResponseEntity<Optional<User>> userResponse = extractUser(authorizationHeader);

        Optional<User> optionalUser = userResponse.getBody();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or unauthorized");
        }

        User user = optionalUser.get();

        if (user.getRatedMovies() == null) {
            user.setRatedMovies(new HashMap<>());
        }

        if (user.getRatedMovies().containsKey(title)) {
            return null;
        }

        user.getRatedMovies().put(title, rating);

        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("Movie rated successfully");
    }

    @GetMapping("/hasRole")
    public ResponseEntity<Boolean> hasRole(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        ResponseEntity<Optional<User>> userResponse = extractUser(authorizationHeader);

        Optional<User> optionalUser = userResponse.getBody();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        User user = optionalUser.get();

        Set<Role> roles = user.getRoles();
        boolean hasRole = roles != null && roles.contains(Role.ROLE_ADMIN);

        return ResponseEntity.ok(hasRole);
    }

    @GetMapping("/getMostLiked")
    public ResponseEntity<String> getMostLiked(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        ResponseEntity<Optional<User>> userResponse = extractUser(authorizationHeader);

        if (userResponse.getBody().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userResponse.getBody().get();
        String[] likedMovies = user.getLikedMovies();

        if (likedMovies.length == 0) {
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(likedMovies.length);
        String randomMovie = likedMovies[index];

        return ResponseEntity.ok(randomMovie);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader){

        ResponseEntity<Optional<User>> userResponse = extractUser(authorizationHeader);

        Optional<User> optionalUser = userResponse.getBody();

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = optionalUser.get();

        UserDTO userDTO = new UserDTO();

        userDTO.setCoins(user.getCoins());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setRatedMovies(user.getRatedMovies());

        return ResponseEntity.ok(userDTO);

    }

    @PostMapping("/add-coin")
    public ResponseEntity<String> addCoin(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int amount) {

        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than 0");
        }

        ResponseEntity<Optional<User>> user = extractUser(authHeader);

        if(user.getBody().isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        User userBody = user.getBody().get();

        userBody.setCoins(userBody.getCoins() + amount);

        userRepo.save(userBody);

        return ResponseEntity.ok("Added " + amount + " coins to user: " + userBody.getEmail());
    }

    @GetMapping("/getRatedMovies")
    public ResponseEntity<List<FileDTO>> getRatedMovies(@RequestHeader("Authorization") String authHeader) {
        ResponseEntity<Optional<User>> user = extractUser(authHeader);

        if(user.getBody().isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        User userBody = user.getBody().get();

        List<FileDTO> ratedMovies = new ArrayList<>();

        userBody.getRatedMovies().forEach((movieTitle, rating) -> {
            FileDTO movieDTO = new FileDTO();
            movieDTO.setTitle(movieTitle);
            movieDTO.setRating(rating);
            ratedMovies.add(movieDTO);
        });

        return ResponseEntity.ok(ratedMovies);
    }

}

class ApiResponse {
    private boolean success;
    private String message;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
