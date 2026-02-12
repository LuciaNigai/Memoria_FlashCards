package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.AccessLevel;
import com.lucia.memoria.mapper.DeckMapper;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.DeckRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service for managing hierarchical Flashcard Decks.
 * Supports a tree-like structure using path-based indexing (e.g., Parent::Child).
 * Handles recursive operations like subtree deletion and path updates.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class DeckService {

  private final DeckRepository deckRepository;
  private final UserService userService;
  private final DeckMapper deckMapper;

  @Transactional
  public DeckResponseDTO createDeck(DeckRequestDTO dto) {
    User user = userService.getUserEntityById(dto.getUserId());
    String dtoName = validateAndTrimName(dto.getName());

    //  Handle Parent & Path Logic
    Deck parent = Optional.ofNullable(dto.getPath())
        .filter(StringUtils::isNotBlank)
        .flatMap(deckRepository::findByPath)
        .orElse(null);

    if (dto.getPath() != null && parent == null) {
      throw new NotFoundException("Parent path not found: " + dto.getPath());
    }

    // Set the new deck path as "parentDeckPath::deckName" to maintain hierarchical structure
    String newPath = Deck.computePath(parent, dtoName);
    validateUniquePath(newPath, user);

    // Determine deck access level based on DTO input and parent's access level
    AccessLevel accessLevel = Deck.determineDeckAccessLevel(dto.getAccessLevel(), parent);

    // Save the deck
    Deck deck = new Deck(user, dtoName, accessLevel, parent, newPath);
    return deckMapper.toDTO(deckRepository.save(deck));
  }

  @Transactional(readOnly = true)
  public Deck getDeckEntityById(UUID deckId) {
    return findDeckOrThrow(deckId);
  }


  @Transactional(readOnly = true)
  public List<DeckResponseDTO> getDecksByUserId(UUID userId) {
    User user = userService.getUserEntityById(userId);
    List<Deck> allDecks = deckRepository.findAllByUser(user);

    // Convert all to DTOs first and map by path
    Map<String, DeckResponseDTO> dtoMap = allDecks.stream()
        .filter(d -> d.getPath() != null)
        .collect(Collectors.toMap(
            Deck::getPath,
            deckMapper::toDTO,
            (existing, replacement) ->existing,
            LinkedHashMap::new
        ));

    // Link children to parents and return only the roots
    return dtoMap.values().stream()
        .filter(dto -> {
          String parentPath = getParentPath(dto.getPath());
          if (parentPath == null || !dtoMap.containsKey(parentPath)) {
            return true; // It's a root
          }
          dtoMap.get(parentPath).getChildDecks().add(dto);
          return false;
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public DeckResponseDTO getDeckById(UUID deckId) {
    return deckMapper.toDTO(findDeckOrThrow(deckId));
  }

  @Transactional
  public void deleteDeck(UUID deckId, boolean force) {
    Deck rootDeck = deckRepository.findByDeckIdWithCards(deckId).orElseThrow(
        () -> new NotFoundException("Deck you are trying to delete does not exist")
    );
    // get all user decks
    List<Deck> allUserDecks = deckRepository.findAllByUser(rootDeck.getUser());
    List<Deck> subtree = getSubtree(allUserDecks, rootDeck);

    //  check for cards if force is false
    if (!force) {
      validateEmptySubtree(subtree);
    }
    //  delete decks
    deckRepository.deleteAll(subtree);
  }

  @Transactional
  public DeckRequestDTO renameDeck(UUID deckId, String name) {
    Deck deck = findDeckOrThrow(deckId);

    String trimmedName = validateAndTrimName(name);

    // Compute new path and validate
    String newPath = Deck.computePath(deck.getParentDeck(), trimmedName);
    validateUniquePath(newPath, deck.getUser(), deck.getDeckId());

    // Update name and path
    deck.setName(name);
    deck.setPath(newPath);

    // update all child paths
    updateChildPaths(deck);
    return deckMapper.toMinimalDTO(deckRepository.save(deck));
  }

  private void updateChildPaths(Deck parentDeck) {
    // Use an iterative Stack (DFS) to update children.
    // This avoids StackOverflowErrors that can occur with deep nesting in recursive calls.
    Deque<Deck> stack = new ArrayDeque<>();
    stack.push(parentDeck);

    List<Deck> updatedChildren = new ArrayList<>();

    while (!stack.isEmpty()) {
      Deck current = stack.pop();

      List<Deck> children = deckRepository.findAllByParentDeck(current);
      for (Deck child : children) {
        // Update path based on parent
        child.setPath(current.getPath() + "::" + child.getName());
        updatedChildren.add(child);
        stack.push(child);
      }
    }

    // Batch save all updated children
    if (!updatedChildren.isEmpty()) {
      deckRepository.saveAll(updatedChildren);
    }
  }

  private static List<Deck> getSubtree(List<Deck> allUserDecks, Deck rootDeck) {
    // Build a parent -> children lookup map for O(1) access during traversal
    Map<UUID, List<Deck>> parentMap = new HashMap<>();
    for (Deck deck : allUserDecks) {
      if (deck.getParentDeck() != null) {
        parentMap.computeIfAbsent(deck.getParentDeck().getDeckId(), k -> new ArrayList<>())
            .add(deck);
      }
    }

    // Iteratively collect the entire subtree using a stack-based Depth-First Search.
    // This approach ensures memory safety for large or deeply nested deck structures.
    List<Deck> decksToDelete = new ArrayList<>();
    Deque<Deck> stack = new ArrayDeque<>();
    stack.push(rootDeck);

    while (!stack.isEmpty()) {
      Deck current = stack.pop();
      decksToDelete.add(current);

      List<Deck> children = parentMap.getOrDefault(current.getDeckId(), List.of());
      stack.addAll(children);
    }
    return decksToDelete;
  }

  private String validateAndTrimName(String name) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("Deck name cannot be empty");
    }
    return name.trim();
  }

  private void validateUniquePath(String path, User user) {
    validateUniquePath(path, user, null);
  }

  private void validateUniquePath(String path, User user, UUID currentId) {
    deckRepository.findByPathAndUser(path, user)
        .filter(d -> !d.getDeckId().equals(currentId))
        .ifPresent(d -> { throw new ConflictWithDataException("Path already exists: " + path); });
  }

  private String getParentPath(String path) {
    // Finds the last occurrence of the separator "::"
    int lastIndex = path.lastIndexOf("::");

    // If "::" isn't found, it's already a root deck (return null)
    // If found, return everything before the last "::"
    return (lastIndex == -1) ? null : path.substring(0, lastIndex);
  }

  /**
   * Validates that none of the decks in the provided list contain cards.
   * @throws ConflictWithDataException if any deck in the subtree has cards.
   */
  private void validateEmptySubtree(List<Deck> subtree) {
    List<UUID> decksWithCards = subtree.stream()
        .filter(deck -> !deck.getCards().isEmpty())
        .map(Deck::getDeckId)
        .toList();

    if (!decksWithCards.isEmpty()) {
      throw new ConflictWithDataException(
          "Cannot delete deck(s) containing cards without force flag.",
          decksWithCards
      );
    }
  }

  // --- Private Helper ---
  // This is the "Source of Truth" for finding a deck.
  // Internal methods call this to avoid the "this" proxy warning.
  private Deck findDeckOrThrow(UUID deckId) {
    return deckRepository.findByDeckId(deckId)
        .orElseThrow(() -> new NotFoundException("Deck not found."));
  }
}
