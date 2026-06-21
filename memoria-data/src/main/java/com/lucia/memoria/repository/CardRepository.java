package com.lucia.memoria.repository;

import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {

  @Query("SELECT c FROM Card c " +
      "LEFT JOIN FETCH c.fields f " +
      "LEFT JOIN FETCH f.templateField tf " +
      "WHERE c.deck = :deck")
  List<Card> findAllByDeck(@Param("deck")Deck deck);

  @Query("SELECT c FROM Card c " +
      "LEFT JOIN FETCH c.fields f " +
      "LEFT JOIN FETCH f.templateField tf " +
      "WHERE c.cardId = :cardId")
  Optional<Card> findByCardIdWithFieldsAndFieldTemplates(@Param("cardId") UUID cardId);

  @Query("SELECT c.cardId FROM Card c " +
      "JOIN c.fields f " +
      "JOIN f.templateField tf " +
      "WHERE tf.fieldRole = :fieldRole AND LOWER(f.content) = LOWER(:content)")
  List<UUID> findCardIdsByFieldRoleAndContent(
      @Param("fieldRole") FieldRole fieldRole,
      @Param("content") String content
  );

  Optional<Card> findByCardId(UUID cardId);

  long countByTemplateTemplateId(UUID templateId);
  @Query("""
    SELECT COUNT(c.id) FROM Card c
    JOIN c.deck d
    WHERE d.path = :rootPath OR d.path LIKE CONCAT(:rootPath, '::%') ESCAPE '\\'
    """)
  long countCardsInSubtree(@Param("rootPath") String rootPath);
}
