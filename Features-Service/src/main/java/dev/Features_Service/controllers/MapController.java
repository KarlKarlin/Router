package dev.Features_Service.controllers;

import dev.Features_Service.dto.UserDTO;
import dev.Features_Service.models.Map;
import dev.Features_Service.repositories.mapRepo;
import dev.Features_Service.services.AuthenticationInterface;
import dev.Features_Service.services.MapService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @Autowired
    private mapRepo mapRepo;

    @Autowired
    AuthenticationInterface authenticationInterface;

    @PostMapping("/createMap")
    public ResponseEntity<String> createMap(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String userRequest) throws IOException {

        mapService.createMap(authorizationHeader, userRequest);

        return ResponseEntity.ok("success");

    }

    @GetMapping("/getMap")
    public ResponseEntity<Map> getMap(@RequestHeader("Authorization") String authorizationHeader){

        return ResponseEntity.ok(mapService.getUserMap(authorizationHeader));

    }

    @DeleteMapping("/deleteMap")
    @Transactional
    public ResponseEntity<String> deleteMap(@RequestHeader("Authorization") String authorizationHeader){

        UserDTO userDTO = Optional.ofNullable(authenticationInterface.getUser(authorizationHeader).getBody())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Map> map = mapRepo.findByUserEmail(userDTO.getEmail());

        if(map.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Map does not exist for this user");

        mapRepo.delete(map.get());

        return ResponseEntity.ok("Map was deleted successfully");

    }

}
