package dev.Features_Service.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.Features_Service.dto.FileDTO;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Map")
@Data
public class Map {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;

    @Lob
    private String relationShipsJson;

    private boolean rated;

    private Date seasonEnd;

    @Transient
    private List<FileDTO> relationShips;

    public void setRelationShips(List<FileDTO> movies) {
        this.relationShips = movies;
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.relationShipsJson = mapper.writeValueAsString(movies);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize movies", e);
        }
    }

    public List<FileDTO> getRelationShips() {
        if (relationShips == null && relationShipsJson != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.relationShips = mapper.readValue(relationShipsJson, new TypeReference<>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize movies", e);
            }
        }
        return relationShips;
    }

}
