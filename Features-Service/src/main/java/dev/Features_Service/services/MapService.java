package dev.Features_Service.services;

import dev.Features_Service.dto.FileDTO;
import dev.Features_Service.dto.UserDTO;
import dev.Features_Service.models.Map;
import dev.Features_Service.repositories.mapRepo;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MapService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    @Autowired
    private AuthenticationInterface authenticationInterface;

    @Autowired
    private FileInterface fileInterface;

    @Autowired
    private mapRepo mapRepo;

    @Transactional
    public void createMap(String authorizationHeader, String userRequest) throws IOException {

        UserDTO userDTO = Optional.ofNullable(authenticationInterface.getUser(authorizationHeader).getBody())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (mapRepo.existsByUserEmail(userDTO.getEmail())) {
            throw new IllegalStateException("Map already exists for this user.");
        }

        String encodedText = URLEncoder.encode(userRequest, StandardCharsets.UTF_8);

        URL url = new URL("http://localhost:5002/predict_genre/?text=" + encodedText);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = in.readLine();
        in.close();

        List<FileDTO> recommendMovies = Optional.ofNullable(fileInterface.recommendByGenre(response).getBody())
                .orElseThrow(() -> new IllegalArgumentException("No recommended movies found."));

        if (recommendMovies.isEmpty()) {
            throw new IllegalArgumentException("No recommended movies found.");
        }

        Map map = new Map();
        map.setUserEmail(userDTO.getEmail());
        map.setRated(false);
        map.setRelationShips(recommendMovies);

        LocalDateTime seasonEnd = LocalDateTime.now().plusMonths(1);
        map.setSeasonEnd(Date.from(seasonEnd.atZone(ZoneId.systemDefault()).toInstant()));

        mapRepo.save(map);

        log.info("Created new map for user: {}", userDTO.getEmail());

    }

    @Transactional
    public Map getUserMap(String authorizationHeader) {
        String email = Optional.ofNullable(authenticationInterface.extractEmail(authorizationHeader).getBody())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        rewardUsersWhoRatedTheirMapMovies(authorizationHeader);

        return mapRepo.findByUserEmail(email)
                .orElseThrow(() -> new IllegalStateException("No map found."));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void removeExpiredMaps() {

        Date now = new Date();
        List<Map> expiredMaps = mapRepo.findAllBySeasonEndBefore(now);

        if (expiredMaps.isEmpty()) return;

        for (Map expiredMap : expiredMaps) {

                mapRepo.delete(expiredMap);

        }

        mapRepo.deleteAll(expiredMaps);
        log.info("{}: Deleted {} expired maps", LocalDateTime.now(), expiredMaps.size());
    }

    public boolean rewardUsersWhoRatedTheirMapMovies(String authHeader) {

        String email = Optional.ofNullable(authenticationInterface.extractEmail(authHeader).getBody())
                .orElse(null);

        if (email == null) return false;

        Optional<Map> mapOpt = mapRepo.findByUserEmail(email);
        if (mapOpt.isEmpty()) return false;

        Map userMap = mapOpt.get();
        List<FileDTO> expectedMovies = userMap.getRelationShips();

        try {

            List<FileDTO> ratedMovies = Optional.ofNullable(authenticationInterface.getRatedMovies(authHeader).getBody())
                    .orElse(List.of());

            log.info("Comparing rated movies with user map for email: {}", email);

            boolean allMatched = expectedMovies.stream().allMatch(expected ->
                    ratedMovies.stream().anyMatch(rated ->
                            rated.getTitle().equalsIgnoreCase(expected.getTitle())
                    )
            );

            if (allMatched) {
                rewardUserByTheEndOfTheSeason(authHeader);
                mapRepo.delete(userMap);
                log.info("User {} was rewarded and map deleted", email);
                return true;
            } else {
                log.info("User {} has not rated all map movies correctly", email);
            }

        } catch (FeignException e) {
            log.error("Feign error: {} - {}", e.status(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during reward process", e);
        }

        return false;
    }

    private void rewardUserByTheEndOfTheSeason(String authHeader) {
        authenticationInterface.addCoin(authHeader, 5);
    }
}
