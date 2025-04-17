package dev.File_Processing.services;

import dev.File_Processing.models.File;
import dev.File_Processing.repositories.fileRepo;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Service
public class RecommendationSyncService {

    @Autowired
    private fileRepo fileRepo;
    private final Driver driver;

    public RecommendationSyncService() {
        this.driver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "sWdhqfbzz")
        );
    }

    public void syncMovie(File file) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                MERGE (m:Movie {title: $title})
                SET m.description = $description,
                    m.rating = $rating,
                    m.ageRating = $ageRating

                WITH m
                UNWIND $genres AS genre
                    MERGE (g:Genre {name: genre})
                    MERGE (m)-[:HAS_GENRE]->(g)

                WITH m
                UNWIND $tags AS tag
                    MERGE (t:Tag {name: tag})
                    MERGE (m)-[:HAS_TAG]->(t)

                WITH m
                UNWIND $cast AS actor
                    MERGE (a:Actor {name: actor})
                    MERGE (m)-[:HAS_ACTOR]->(a)

                WITH m
                UNWIND $directors AS director
                    MERGE (d:Director {name: director})
                    MERGE (m)-[:HAS_DIRECTOR]->(d)

                WITH m
                MERGE (c:Country {name: $country})
                MERGE (m)-[:HAS_COUNTRY]->(c)
            """, parameters(
                        "title", file.getTitle(),
                        "description", file.getDescription(),
                        "rating", file.getRating(),
                        "ageRating", file.getAgeRating(),
                        "genres", List.of(file.getGenre()),
                        "tags", List.of(file.getTags()),
                        "cast", List.of(file.getCast()),
                        "directors", List.of(file.getDirectors()),
                        "country", file.getCountry()
                ));
                return null;
            });
        }
    }

    public List<File> recommendFiles(String movieTitle) {
        try (Session session = driver.session()) {
            List<String> titles = session.readTransaction(tx -> {
                var result = tx.run("""
                MATCH (m:Movie {title: $title})-[:HAS_TAG|HAS_GENRE|HAS_ACTOR|HAS_DIRECTOR|HAS_COUNTRY]->(x)
                      <-[:HAS_TAG|HAS_GENRE|HAS_ACTOR|HAS_DIRECTOR|HAS_COUNTRY]-(rec:Movie)
                WHERE rec.title <> $title
                RETURN rec.title AS title
                LIMIT 10
            """, parameters("title", movieTitle));

                return result.stream()
                        .map(record -> record.get("title").asString())
                        .toList();
            });

            return fileRepo.findByTitleIn(titles);
        }
    }

    public List<File> searchMovies(String keyword) {
        try (Session session = driver.session()) {
            List<String> titles = session.readTransaction(tx -> {
                var result = tx.run("""
                MATCH (m:Movie)
                WHERE toLower(m.title) CONTAINS toLower($keyword)
                OR EXISTS {
                    MATCH (m)-[:HAS_GENRE]->(g:Genre)
                    WHERE toLower(g.name) CONTAINS toLower($keyword)
                }
                OR EXISTS {
                    MATCH (m)-[:HAS_TAG]->(t:Tag)
                    WHERE toLower(t.name) CONTAINS toLower($keyword)
                }
                OR EXISTS {
                    MATCH (m)-[:HAS_ACTOR]->(a:Actor)
                    WHERE toLower(a.name) CONTAINS toLower($keyword)
                }
                OR EXISTS {
                    MATCH (m)-[:HAS_DIRECTOR]->(d:Director)
                    WHERE toLower(d.name) CONTAINS toLower($keyword)
                }
                OR EXISTS {
                    MATCH (m)-[:HAS_COUNTRY]->(c:Country)
                    WHERE toLower(c.name) CONTAINS toLower($keyword)
                }
                RETURN DISTINCT m.title AS title
                LIMIT 20
            """, parameters("keyword", keyword));

                return result.stream()
                        .map(record -> record.get("title").asString())
                        .toList();
            });

            return fileRepo.findByTitleIn(titles);
        }
    }

    public List<File> recommendFilesByGenreName(String genreName) {
        try (Session session = driver.session()) {
            List<String> titles = session.readTransaction(tx -> {
                var result = tx.run("""
                MATCH (g:Genre {name: $genre})<-[:HAS_GENRE]-(rec:Movie)
                RETURN rec.title AS title
                LIMIT 10
            """, parameters("genre", genreName));

                return result.stream()
                        .map(record -> record.get("title").asString())
                        .toList();
            });

            return fileRepo.findByTitleIn(titles);
        }
    }



}

