package dev.File_Processing.services;

import dev.File_Processing.models.File;
import dev.File_Processing.repositories.fileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class fileService {

    private final fileRepo fileRepo;
    private static final String FOLDER_PATH = "/Users/artempodrezov/IdeaProjects/uploads/";

    @Autowired
    AuthenticationInterface authenticationInterface;


    public fileService(fileRepo fileRepo) {
        this.fileRepo = fileRepo;
    }

    public File addFile(MultipartFile multipartFile, String title, Date release_year, String description,
                        String genre, String duration, String cast, String tags, String ageRating,
                        String directors, String country) throws Exception {

        Path uploadDir = Path.of(FOLDER_PATH);

        String uniqueFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        Path filePath = uploadDir.resolve(uniqueFileName);

        Files.write(filePath, multipartFile.getBytes(), StandardOpenOption.CREATE_NEW);

        Thread.sleep(1000);

        String pythonServiceUrl = "http://localhost:5001/process";
        RestTemplate restTemplate = new RestTemplate();

        ByteArrayResource fileResource = new ByteArrayResource(multipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);


        ResponseEntity<Map> response = restTemplate.exchange(pythonServiceUrl, HttpMethod.POST, request, Map.class);

        String vrVideoPath = "null";
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            vrVideoPath = (String) response.getBody().get("path");
        } else {
            throw new IOException("Error in VR processing: " + response.getStatusCode());
        }

        File file = saveFileMetadata(multipartFile, filePath, title, release_year, description, genre,
                duration, cast, tags, ageRating, directors, country, vrVideoPath);

        return file;
    }


    private String authenticateUser(String authorizationHeader) {
        ResponseEntity<String> response = authenticationInterface.extractEmail(authorizationHeader);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("Unauthorized: Failed to extract user email");
        }
        return response.getBody();
    }

    private File saveFileMetadata(MultipartFile multipartFile,
                                  Path filePath,
                                  String title,
                                  Date release_year,
                                  String description,
                                  String genre,
                                  String duration,
                                  String cast,
                                  String tags,
                                  String ageRating,
                                  String directors,
                                  String country,
                                  String vrFilePath) {

        File file = new File();
        file.setTitle(title);
        file.setFileType(multipartFile.getContentType());
        file.setRelease_year(release_year);
        file.setFilePath(filePath.toString());
        file.setDescription(description);
        file.setGenre(genre);
        file.setDuration(duration);
        file.setCast(getArray(cast));
        file.setTags(getArray(tags));
        file.setAgeRating(ageRating);
        file.setDirectors(getArray(directors));
        file.setCountry(country);
        file.setVrFilePath(vrFilePath);

        return fileRepo.save(file);
    }

    private String[] getArray(String string) {
        return Arrays.stream(string.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }
}
