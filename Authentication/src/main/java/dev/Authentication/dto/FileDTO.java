package dev.Authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileDTO implements Serializable {

    private UUID id;
    private String title;
    private float rating;
    private String genre;

}

