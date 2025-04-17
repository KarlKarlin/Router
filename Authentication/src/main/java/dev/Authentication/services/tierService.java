package dev.Authentication.services;

import dev.Authentication.models.User;
import dev.Authentication.repositories.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class tierService {

    @Autowired
    private userRepo userRepo;

    public ResponseEntity<String> assignTier(User user) {
        String tier = "";

        if (user.getCoins() >= 0 && user.getCoins() < 3) {
            tier = assignMoviegoer(user);
        } else if (user.getCoins() >= 3 && user.getCoins() < 5) {
            tier = assignFilmBuff(user);
        } else if (user.getCoins() >= 5 && user.getCoins() < 7) {
            tier = assignExplorer(user);
        } else if (user.getCoins() >= 7 && user.getCoins() < 10) {
            tier = assignMapWanderer(user);
        } else if (user.getCoins() >= 10 && user.getCoins() < 15) {
            tier = assignCinephile(user);
        } else if (user.getCoins() >= 15 && user.getCoins() < 25) {
            tier = assignCurator(user);
        } else if (user.getCoins() >= 25 && user.getCoins() < 40) {
            tier = assignScreenLegend(user);
        } else if (user.getCoins() >= 40) {
            tier = assignMapMaster(user);
        } else {
            tier = assignUntitledTraveler(user);
        }

        user.setTier(tier);
        userRepo.save(user);

        return ResponseEntity.ok("Tier assigned: " + tier);
    }

    private String assignUntitledTraveler(User user) {
        user.setTier("🎬 Untitled Traveler");
        return "🎬 Untitled Traveler";
    }

    private String assignMoviegoer(User user) {
        user.setTier("📽️ Moviegoer");
        return "📽️ Moviegoer";
    }

    private String assignFilmBuff(User user) {
        user.setTier("🍿 Film Buff");
        return "🍿 Film Buff";
    }

    private String assignExplorer(User user) {
        user.setTier("🧳 Explorer");
        return "🧳 Explorer";
    }

    private String assignMapWanderer(User user) {
        user.setTier("🗺️ Map Wanderer");
        return "🗺️ Map Wanderer";
    }

    private String assignCinephile(User user) {
        user.setTier("🧠 Cinephile");
        return "🧠 Cinephile";
    }

    private String assignCurator(User user) {
        user.setTier("🎞️ Curator");
        return "🎞️ Curator";
    }

    private String assignScreenLegend(User user) {
        user.setTier("🦸‍♂️ Screen Legend");
        return "🦸‍♂️ Screen Legend";
    }

    private String assignMapMaster(User user) {
        user.setTier("🌌 Map Master");
        return "🌌 Map Master";
    }
}
