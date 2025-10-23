package com.lucia.memoria.model;

import com.lucia.memoria.helper.AccessLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;
import java.util.UUID;

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

  public Deck(UUID deckId, User user, String name, AccessLevel accessLevel, Deck parentDeck,
      String path) {
    this.deckId = deckId;
    this.user = user;
    this.name = name;
    this.accessLevel = accessLevel;
    this.parentDeck = parentDeck;
    this.path = path;
  }
}
