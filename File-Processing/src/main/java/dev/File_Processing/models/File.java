package dev.File_Processing.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "File")
@Data
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "title", unique = true)
    private String title;

    @Column(name = "release_year")
    private Date release_year;

    @Column(name = "description")
    private String description;

    @Column(name = "genre")
    private String genre;

    @Column(name = "actor_cast", columnDefinition = "TEXT[]")
    private String[] cast;

    @Column(name = "duration")
    private String duration;

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(name = "fileType")
    private String fileType;

    @Column(name = "filePath")
    private String filePath;

    @Column(name = "rating")
    private float rating;

    @Column(name = "votesAmount")
    private int votesAmount;

    @Column(name = "country")
    private String country;

    @Column(name = "ageRating")
    private String ageRating;

    @Column(name = "directors", columnDefinition = "TEXT[]")
    private String[] directors;

    @Column(name = "vrFilePath")
    private String vrFilePath;
}
