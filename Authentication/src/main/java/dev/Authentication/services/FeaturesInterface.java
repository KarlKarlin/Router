package dev.Authentication.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("FEATURES-SERVICE")
public interface FeaturesInterface {

    @DeleteMapping("/map/deleteMap")
    public ResponseEntity<String> deleteMap(@RequestHeader("Authorization") String authorizationHeader);

}
