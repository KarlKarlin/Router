package dev.Features_Service.repositories;

import dev.Features_Service.models.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface mapRepo extends JpaRepository<Map, UUID> {

    boolean existsByUserEmail(String email);
    Optional<Map> findByUserEmail(String email);
    List<Map> findAllBySeasonEndBefore(Date date);

}
