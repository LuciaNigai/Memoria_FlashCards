package com.lucia.memoria.service.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.helper.AccessLevel;
import com.lucia.memoria.mapper.DeckMapper;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.DeckRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckService Unit Tests")
class DeckServiceTest {

  @Mock
  private DeckRepository deckRepository;
  @Mock
  private UserService userService;
  @Mock
  private DeckMapper deckMapper;

  @InjectMocks
  private DeckService deckService;

  private UUID userId;
  private User user;
  private UUID deckId;
  private Deck deck;
  private DeckRequestDTO deckRequestDTO;
  private DeckResponseDTO deckResponseDTO;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = new User();
    user.setUserId(userId);

    deckId = UUID.randomUUID();
    deck = new Deck();
    deck.setDeckId(deckId);
    deck.setName("Test Deck");
    deck.setUser(user);
    deck.setPath("Test Deck");

    deckRequestDTO = new DeckRequestDTO();
    deckRequestDTO.setUserId(userId);
    deckRequestDTO.setName("Test Deck");

    deckResponseDTO = new DeckResponseDTO();
    deckResponseDTO.setId(deckId);
    deckResponseDTO.setName("Test Deck");
    deckResponseDTO.setPath("Test Deck");
    deckResponseDTO.setChildDecks(new ArrayList<>());
  }

  @Test
  @DisplayName("Should successfully create a root deck")
  void createDeck_success_root() {
    // Arrange
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(deckRepository.findByPathAndUser(anyString(), eq(user))).thenReturn(Optional.empty());
    when(deckRepository.save(any(Deck.class))).thenReturn(deck);
    when(deckMapper.toDTO(deck)).thenReturn(deckResponseDTO);

    // Act
    DeckResponseDTO result = deckService.createDeck(deckRequestDTO);

    // Assert
    assertNotNull(result);
    assertEquals("Test Deck", result.getName());
    verify(deckRepository).save(any(Deck.class));
  }

  @Test
  @DisplayName("Should successfully create a child deck with correct path")
  void createDeck_success_child() {
    // Arrange
    deckRequestDTO.setPath("Parent");
    Deck parent = new Deck();
    parent.setPath("Parent");
    parent.setAccessLevel(AccessLevel.PRIVATE);

    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(deckRepository.findByPath("Parent")).thenReturn(Optional.of(parent));
    when(deckRepository.save(any(Deck.class))).thenReturn(deck);
    when(deckMapper.toDTO(deck)).thenReturn(deckResponseDTO);

    // Act
    deckService.createDeck(deckRequestDTO);

    // Assert
    verify(deckRepository).save(argThat(d -> d.getPath().equals("Parent::Test Deck")));
  }

  @Test
  @DisplayName("Should successfully retrieve decks in a hierarchical structure")
  void getDecksByUserId_hierarchical_success() {
    // Arrange
    Deck child = new Deck();
    child.setDeckId(UUID.randomUUID());
    child.setName("Child");
    child.setPath("Parent::Child");
    
    DeckResponseDTO parentDto = new DeckResponseDTO();
    parentDto.setPath("Parent");
    parentDto.setChildDecks(new ArrayList<>());

    DeckResponseDTO childDto = new DeckResponseDTO();
    childDto.setPath("Parent::Child");
    childDto.setChildDecks(new ArrayList<>());

    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(deckRepository.findAllByUser(user)).thenReturn(List.of(deck, child));
    
    // Simulate mapping by paths
    deck.setPath("Parent"); // Change path for root test
    when(deckMapper.toDTO(deck)).thenReturn(parentDto);
    when(deckMapper.toDTO(child)).thenReturn(childDto);

    // Act
    List<DeckResponseDTO> results = deckService.getDecksByUserId(userId);

    // Assert
    assertEquals(1, results.size());
    assertEquals(1, results.getFirst().getChildDecks().size());
  }

  @Test
  @DisplayName("Should successfully delete a deck and its subtree when force flag is true")
  void deleteDeck_success_forceTrue() {
    // Arrange
    when(deckRepository.findByDeckIdWithCards(deckId)).thenReturn(Optional.of(deck));
    when(deckRepository.findAllByUser(user)).thenReturn(List.of(deck));

    // Act
    deckService.deleteDeck(deckId, true);

    // Assert
    verify(deckRepository).deleteAll(anyList());
  }

  @Test
  @DisplayName("Should throw ConflictWithDataException when deleting a deck with cards without force flag")
  void deleteDeck_withCards_noForce_throwsException() {
    // Arrange
    deck.setCards(List.of(mock(com.lucia.memoria.model.Card.class)));
    when(deckRepository.findByDeckIdWithCards(deckId)).thenReturn(Optional.of(deck));
    when(deckRepository.findAllByUser(user)).thenReturn(List.of(deck));

    // Act & Assert
    assertThrows(ConflictWithDataException.class, () -> deckService.deleteDeck(deckId, false));
  }

  @Test
  @DisplayName("Should successfully rename a deck and update its path")
  void renameDeck_success() {
    // Arrange
    when(deckRepository.findByDeckId(deckId)).thenReturn(Optional.of(deck));
    when(deckRepository.save(any(Deck.class))).thenReturn(deck);
    when(deckMapper.toMinimalDTO(deck)).thenReturn(deckRequestDTO);

    // Act
    deckService.renameDeck(deckId, "New Name");

    // Assert
    assertEquals("New Name", deck.getName());
    verify(deckRepository).save(deck);
  }
}
