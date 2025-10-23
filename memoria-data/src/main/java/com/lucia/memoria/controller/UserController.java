package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.DeckDTO;
import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.service.local.DeckService;
import com.lucia.memoria.service.local.TemplateService;
import com.lucia.memoria.service.local.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data/users")
public class UserController {

  private final UserService userService;
  private final DeckService deckService;
  private final TemplateService templateService;

  public UserController(UserService userService, DeckService deckService,
      TemplateService templateService) {
    this.userService = userService;
    this.deckService = deckService;
    this.templateService = templateService;
  }

  @Operation(summary = "Create new user")
  @ApiResponse(responseCode = "200", description = "Created user", content = {
      @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))})
  @PostMapping
  public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
    UserDTO savedUser = userService.createUser(userDTO);
    return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
  }

  @Operation(summary = "Get user by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))}),
      @ApiResponse(responseCode = "400", description = "User not found", content = @Content)})
  @GetMapping("/{userId}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable("userId") UUID userId) {
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(user);
  }

  @Operation(summary = "Get all users")
  @ApiResponse(responseCode = "200", description = "Returns a list of all users", content = {
      @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))})
  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{userId}/templates")
  public ResponseEntity<List<TemplateDTO>> getUserTemplates(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok().body(templateService.getTemplatesByUserId(userId));
  }

  @GetMapping("/{userId}/decks")
  public ResponseEntity<List<DeckDTO>> getUserDecks(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok().body(deckService.getDecksByUserId(userId));
  }
}
