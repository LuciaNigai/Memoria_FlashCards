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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DeckService {

  private final DeckRepository deckRepository;
  private final UserService userService;
  private final DeckMapper deckMapper;

  public DeckService(DeckRepository deckRepository, UserService userService,
      DeckMapper deckMapper) {
    this.deckRepository = deckRepository;
    this.userService = userService;
    this.deckMapper = deckMapper;
  }

  @Transactional
  public DeckResponseDTO createDeck(DeckRequestDTO dto) {
    Deck parent = null;
    String path = dto.getPath();

    User user = userService.getUserEntityById(dto.getUserId());
//   Validate parent path exists to correctly attach the new deck in the hierarchy
    if (StringUtils.isNotBlank(path)) {
      parent = deckRepository.findByPath(path)
          .orElseThrow(() -> new NotFoundException("Parent path was not found"));
    }

    String dtoName = Optional.ofNullable(dto.getName())
        .orElseThrow(() -> new IllegalArgumentException("The deck name cannot be empty")).trim();
//    Set the new deck path as "parentDeckPath::deckName" to maintain hierarchical structure
    String newPath = parent == null ? dtoName : path.trim() + "::" + dtoName;
//    check for duplicates
    Optional<Deck> duplicateDeck = deckRepository.findByPathAndUser(newPath, user);
    if (duplicateDeck.isPresent()) {
      throw new ConflictWithDataException("Deck already exists",
          List.of(deckMapper.toDTO(duplicateDeck.get())));
    }
//    Determine deck access level based on DTO input and parent's access level
    AccessLevel accessLevel = determineDeckAccessLevel(dto.getAccessLevel(), parent);

    Deck deck = new Deck(UUID.randomUUID(), user, dtoName, accessLevel, parent, newPath);
    Deck saved = deckRepository.save(deck);
    log.info("Deck saved {}", saved);
    return deckMapper.toDTO(saved);
  }

  @Transactional(readOnly = true)
  public Deck getDeckEntityById(UUID deckId) {
    return deckRepository.findByDeckId(deckId)
        .orElseThrow(() -> new NotFoundException("Deck not found."));
  }

  @Transactional(readOnly = true)
  public List<DeckResponseDTO> getDecksByUserId(UUID userId) {
    User user = userService.getUserEntityById(userId);

    List<Deck> allDecks = deckRepository.findAllByUser(user);
    Map<String, DeckResponseDTO> map = new LinkedHashMap<>();

    for (Deck deck : allDecks) {
      String path = deck.getPath();
      if (path == null) {
        log.warn("Deck with null path found: {}", deck.getDeckId());
        continue;
      }
      DeckResponseDTO dto = deckMapper.toDTO(deck);
      dto.setChildDecks(new ArrayList<>());
      map.put(path, dto);
    }

    List<DeckResponseDTO> roots = new ArrayList<>();

    return constructDeckTree(allDecks, map, roots);
  }

  @Transactional(readOnly = true)
  public DeckResponseDTO getDeckById(UUID deckID) {
    return deckMapper.toDTO(getDeckEntityById(deckID));
  }

  @Transactional
  public void deleteDeck(UUID deckId, boolean force) {
    Deck rootDeck = deckRepository.findByDeckIdWithCards(deckId).orElseThrow(
        () -> new NotFoundException("Deck you are trying to delete does not exist")
    );
//    get all user decks
    List<Deck> allUserDecks = deckRepository.findAllByUser(rootDeck.getUser());

    List<Deck> decksToDelete = getDeckWithChildren(allUserDecks, rootDeck);
//    check for cards if force is false
    if (!force) {
      List<UUID> decksWithCards = decksToDelete.stream()
          .filter(d -> !d.getCards().isEmpty())
          .map(Deck::getDeckId)
          .toList();
      if (!decksWithCards.isEmpty()) {
        throw new ConflictWithDataException(
            "Cannot delete deck(s) containing cards without force flag.", decksWithCards);
      }
    }
//    delete decks
    decksToDelete.forEach(d -> log.info("Deleting deck: {} - {}", d.getDeckId(), d.getName()));
    deckRepository.deleteAll(decksToDelete);
  }

  @Transactional
  public DeckRequestDTO renameDeck(UUID deckId, String name) {
    Deck deck = deckRepository.findByDeckId(deckId).orElseThrow(
        () -> new NotFoundException("Deck you are trying to update does not exist")
    );
    if (name == null || name.isBlank()) {
      throw new ConflictWithDataException("Deck name cannot be empty");
    }
    // Compute new path
    String newPath = (deck.getParentDeck() == null ?
        name.trim()
        : deck.getParentDeck().getPath() + "::" + name).trim();

    // Check for duplicates
    Optional<Deck> duplicateDeck = deckRepository.findByPathAndUser(newPath, deck.getUser());
    if (duplicateDeck.isPresent() && !duplicateDeck.get().getDeckId().equals(deck.getDeckId())) {
      throw new ConflictWithDataException("Deck already exists",
          List.of(deckMapper.toDTO(duplicateDeck.get())));
    }

    // Update name and path
    deck.setName(name);
    deck.setPath(newPath);

    // update all child paths
    updateChildPaths(deck);

    return deckMapper.toMinimalDTO(deckRepository.save(deck));
  }

  private void updateChildPaths(Deck parentDeck) {
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

  /**
   * Determines the effective access level for a deck. Rules: 1. If dtoAccessLevel is DEFAULT: - If
   * parent is null -> PRIVATE - If parent is not null -> inherit parent's access level 2. If
   * dtoAccessLevel is not DEFAULT: - Use dtoAccessLevel if not null - Otherwise default to PRIVATE
   *
   * @param dtoAccessLevel the access level from DTO
   * @param parent         the parent deck, may be null
   * @return the effective access level for the new deck
   */
  private AccessLevel determineDeckAccessLevel(AccessLevel dtoAccessLevel, Deck parent) {
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

  private static List<DeckResponseDTO> constructDeckTree(List<Deck> allDecks, Map<String, DeckResponseDTO> map,
      List<DeckResponseDTO> roots) {
    for (Deck deck : allDecks) {
      String path = deck.getPath();
      DeckResponseDTO currentDTO = map.get(path);
      Deck parentDeck = deck.getParentDeck();

      if (parentDeck == null || parentDeck.getPath() == null) {
        roots.add(currentDTO);
      } else {
        DeckResponseDTO parentDTO = map.get(parentDeck.getPath());
        if (parentDTO != null) {
          parentDTO.getChildDecks().add(currentDTO);
        } else {
          log.warn("Parent deck missing for: {}", path);
          roots.add(currentDTO);
        }
      }
    }

    return roots;
  }

  private static List<Deck> getDeckWithChildren(List<Deck> allUserDecks, Deck rootDeck) {
    //    build a parent ->  children map for quick lookup
    Map<UUID, List<Deck>> parentMap = new HashMap<>();
    for (Deck deck : allUserDecks) {
      if (deck.getParentDeck() != null) {
        parentMap.computeIfAbsent(deck.getParentDeck().getDeckId(), k -> new ArrayList<>())
            .add(deck);
      }
    }

    // Iteratively collect all decks in the subtree
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
}
