package dev.File_Processing.repositories;

import dev.File_Processing.models.File;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Repository
public interface fileRepo extends JpaRepository<File, UUID> {

    List<File> findByTitleIn(List<String> titles);

    List<File> findTop10ByRatingGreaterThanOrderByRatingDesc(float rating);

    Page<File> findAll(Pageable pageable);


}
