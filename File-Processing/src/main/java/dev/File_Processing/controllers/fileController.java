package dev.File_Processing.controllers;

import dev.File_Processing.models.File;
import dev.File_Processing.repositories.fileRepo;
import dev.File_Processing.services.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class fileController {

    private final fileService fileService;
    private final fileRepo fileRepo;

    @Autowired
    AuthenticationInterface authenticationInterface;

    @Autowired
    private RecommendationSyncService recommendationService;


    public fileController(fileService fileService, fileRepo fileRepo) {
        this.fileService = fileService;
        this.fileRepo = fileRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile,
                                                  @RequestParam("title") String title,
                                                  @RequestParam("release_year") @DateTimeFormat(pattern = "yyyy-MM-dd") Date release_year,
                                                  @RequestParam("description") String description,
                                                  @RequestParam("genre") String genre,
                                                  @RequestParam("duration") String duration,
                                                  @RequestParam("cast") String cast,
                                                  @RequestParam("tags") String tags,
                                                  @RequestParam("ageRating") String ageRating,
                                                  @RequestParam("directors") String directors,
                                                  @RequestParam("country") String country,
                                                  @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {

            if (Boolean.FALSE.equals(authenticationInterface.hasRole(authorizationHeader).getBody())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Access denied: ADMIN role required"));
            }

            File savedFile = fileService.addFile(multipartFile, title, release_year, description, genre, duration, cast, tags, ageRating, directors, country);

            recommendationService.syncMovie(savedFile);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "File has been uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<Resource> getVideo(@PathVariable UUID movieId, @RequestHeader HttpHeaders headers) {
        try {
            Optional<File> file = fileRepo.findById(movieId);
            if (file.isEmpty()) throw new IllegalArgumentException("File does not exist");

            File fileBody = file.get();
            Path path = Path.of(fileBody.getFilePath());

            long fileLength = Files.size(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "application/octet-stream";

            String range = headers.getFirst(HttpHeaders.RANGE);
            if (range == null) {

                Resource resource = new FileSystemResource(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileLength)
                        .body(resource);
            }

            String[] ranges = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty()) ? Long.parseLong(ranges[1]) : fileLength - 1;

            if (end >= fileLength) end = fileLength - 1;

            long contentLength = end - start + 1;

            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(start);

            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .body(inputStreamResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/vr/{movieId}")
    public ResponseEntity<Resource> getVrVideo(@PathVariable UUID movieId, @RequestHeader HttpHeaders headers) { //testcase

        try {

            Optional<File> file = fileRepo.findById(movieId);
            if (file.isEmpty()) throw new IllegalArgumentException("File does not exist");

            File fileBody = file.get();

            String vrFilePath = fileBody.getVrFilePath();
            if (vrFilePath == null || vrFilePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Path path = Path.of(vrFilePath);
            long fileLength = Files.size(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "application/octet-stream";

            String range = headers.getFirst(HttpHeaders.RANGE);
            if (range == null) {

                Resource resource = new FileSystemResource(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileLength)
                        .body(resource);
            }

            String[] ranges = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty()) ? Long.parseLong(ranges[1]) : fileLength - 1;

            if (end >= fileLength) end = fileLength - 1;

            long contentLength = end - start + 1;

            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(start);

            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .body(inputStreamResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


    @DeleteMapping("/delete/{movieId}")
    @Transactional
    public ResponseEntity<ApiResponse> deleteMovie(@PathVariable UUID movieId, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws IOException {

        if (Boolean.FALSE.equals(authenticationInterface.hasRole(authorizationHeader).getBody())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Access denied: ADMIN role required"));
        }

        Optional<File> file = fileRepo.findById(movieId);

        if (file.isEmpty()) throw new IllegalArgumentException("File does not exist");

        File fileBody = file.get();

        String filePath = fileBody.getFilePath();
        String vrFilePath = fileBody.getVrFilePath();

        Files.delete(Path.of(filePath));
        Files.delete(Path.of(vrFilePath));

        fileRepo.delete(fileBody);

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Movie has been deleted successfully"));
    }

    @PostMapping("/likeMovie/{movieId}")
    public ResponseEntity<?> likeMovie(@PathVariable UUID movieId, @RequestParam float stars, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        try {

            Optional<File> fileOptional = fileRepo.findById(movieId);

            if (fileOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File does not exist");
            }

            File file = fileOptional.get();

            // Handle liked movie logic based on rating
            if (stars > 3) {
                authenticationInterface.setLikedMovie(authorizationHeader, file.getTitle());
            }

            if(authenticationInterface.addRatedMovie(authorizationHeader, file.getTitle(), stars).getBody() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");

            // Add rated movie to the user's list
            authenticationInterface.addRatedMovie(authorizationHeader, file.getTitle(), stars);

            // Ensure stars is within valid range
            if (stars < 0 || stars > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stars must be between 0 and 5");
            }

            // Update the rating and votes count
            int votesAmount = file.getVotesAmount(); // Ensure this is an integer value
            float currentRating = file.getRating();

            // Calculate the new rating
            float newRating = (currentRating * votesAmount + stars) / (votesAmount + 1);

            // Update the movie's rating and increase votes count
            file.setRating(newRating);
            file.setVotesAmount(votesAmount + 1); // Increase the vote count

            fileRepo.save(file);

            return ResponseEntity.ok().body("Movie was successfully rated");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again later");
        }

    }

    @GetMapping("/getRecommendations")
    public ResponseEntity<List<File>> recommendMovies(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        try {

            String likedMovie = authenticationInterface.getMostLiked(authorizationHeader).getBody();

            if (likedMovie == null) return getTopMovies();

            return ResponseEntity.ok().body(recommendationService.recommendFiles(likedMovie));

        }catch (Exception e){
            return ResponseEntity.ok(fileRepo.findAll(PageRequest.of(0, 10)).getContent());
        }
    }

    @GetMapping("/getTopMovies")
    public ResponseEntity<List<File>> getTopMovies(){

        List<File> topRated = fileRepo.findTop10ByRatingGreaterThanOrderByRatingDesc(4.0f);

        if (topRated.size() < 10) {
            List<File> fallback = fileRepo.findTop10ByRatingGreaterThanOrderByRatingDesc(0f);
            return ResponseEntity.ok().body(fallback);
        }

        return ResponseEntity.ok().body(topRated);
    }

    @GetMapping("/getMovieById")
    public ResponseEntity<String> getMovieById(UUID movieId){

        Optional<File> file = fileRepo.findById(movieId);

        return file.map(value -> ResponseEntity.ok(value.getTitle())).orElse(null);

    }

    @GetMapping("/recommendByGenre")
    public ResponseEntity<List<File>> recommendByGenre(@RequestParam String genre) {

        genre = genre.replaceAll("^\"|\"$|^'|'$", "");

        return ResponseEntity.ok(recommendationService.recommendFilesByGenreName(genre));

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
