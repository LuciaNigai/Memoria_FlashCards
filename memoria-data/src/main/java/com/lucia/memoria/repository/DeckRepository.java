package com.lucia.memoria.repository;

import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface DeckRepository extends JpaRepository<Deck, Long> {

  Optional<Deck> findByPathAndUser(String path, User user);

  Optional<Deck> findByPath(String path);

  List<Deck> findAllByUser(User user);

  Optional<Deck> findByDeckId(UUID deckId);

  @Query("""
      SELECT d FROM Deck d 
      LEFT JOIN FETCH d.cards 
      WHERE d.deckId = :deckId
      """)
  Optional<Deck> findByDeckIdWithCards(@Param("deckId") UUID deckId);

  List<Deck> findAllByParentDeck(Deck parentDeck);

  boolean existsByUser(User user);

  @Query("""
    SELECT d FROM Deck d 
    WHERE d.path = :rootPath OR d.path LIKE CONCAT(:rootPath, '::%') ESCAPE '\\'
    """)
  List<Deck> findSubtreeByPath(@Param("rootPath") String rootPath);

  @Modifying(clearAutomatically = true) // Flushes stale memory state
  @Transactional
  @Query("""
      UPDATE Deck d
      SET d.path = CONCAT(:newPath, SUBSTRING(d.path, LENGTH(:oldPath) + 1))
      WHERE d.path LIKE CONCAT(:oldPath, '::%') ESCAPE '\\'
      """)
  int updateSubtreePaths(@Param("oldPath") String oldPath, @Param("newPath") String newPath);

}
