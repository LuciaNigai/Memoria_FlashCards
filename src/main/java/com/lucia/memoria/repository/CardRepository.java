package com.lucia.memoria.repository;

import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.Tag;
import com.lucia.memoria.model.Template;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {

  List<Card> findAllByDeck(Deck deck);

  @Query("SELECT c FROM Card c " +
      "LEFT JOIN FETCH c.fields f " +
      "LEFT JOIN FETCH f.templateField tf " +
      "WHERE c.cardId = :cardId")
  Optional<Card> findByCardIdWithFieldsAndFieldTemplates(@Param("cardId") UUID cardId);

  @Query("SELECT c.cardId FROM Card c " +
      "JOIN c.fields f " +
      "WHERE f.content = :content")
  List<UUID> findCardIdsByFieldContent(@Param("content") String content);

  Optional<Card> findByCardId(UUID cardId);

  List<Card> findByTemplate(Template template);
}
