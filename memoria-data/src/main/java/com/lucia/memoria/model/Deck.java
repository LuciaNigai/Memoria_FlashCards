package com.lucia.memoria.model;

import com.lucia.memoria.helper.AccessLevel;
import jakarta.persistence.*;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "decks")
public class Deck {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "deck_id", nullable = false, unique = true, updatable = false)
  private UUID deckId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  private User user;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "access_level", nullable = false)
  private AccessLevel accessLevel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", referencedColumnName = "id")
  private Deck parentDeck;

  private String path;

  @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Card> cards;

  public Deck(User user, String name, AccessLevel accessLevel, Deck parentDeck,
      String path) {
    this.deckId = UUID.randomUUID();
    this.user = user;
    this.name = name;
    this.accessLevel = accessLevel;
    this.parentDeck = parentDeck;
    this.path = path;
  }

  /**
   * Computes the hierarchical path string.
   * Logic: If parent exists, "parentPath::name". If no parent, just "name".
   */
  public static String computePath(Deck parent, String name) {
    if (parent == null || StringUtils.isBlank(parent.getPath())) {
      return name.trim();
    }
    return (parent.getPath().trim() + "::" + name.trim());
  }

  /**
   * Determines the effective access level for a deck. Rules:
   * 1. If dtoAccessLevel is DEFAULT:
   *    - If parent is null -> PRIVATE
   *    - If parent is not null -> inherit parent's access level
   * 2. If dtoAccessLevel is not DEFAULT:
   *    - Use dtoAccessLevel if not null
   *    - Otherwise default to PRIVATE
   *
   * @param dtoAccessLevel the access level from DTO
   * @param parent         the parent deck, may be null
   * @return the effective access level for the new deck
   */
  public static AccessLevel determineDeckAccessLevel(AccessLevel dtoAccessLevel, Deck parent) {
    if (dtoAccessLevel == AccessLevel.DEFAULT) {
      if (parent == null) {
        return AccessLevel.PRIVATE;
      } else {
        return parent.getAccessLevel();
      }
    } else {
      return Optional.ofNullable(dtoAccessLevel).orElse(AccessLevel.PRIVATE);
    }
  }
}
