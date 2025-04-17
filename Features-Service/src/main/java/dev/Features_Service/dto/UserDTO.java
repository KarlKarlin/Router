package dev.Features_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {

    private String username;
    private String email;
    private HashMap<String, Float> ratedMovies;
    private int coins;

}
