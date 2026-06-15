package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.RenameRequestDTO;
import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.service.local.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data/tags")
@AllArgsConstructor
@Tag(name = "Tags", description = "Endpoints for managing card tags")
public class TagController {

  private final TagService tagService;

  @PostMapping("/{userId}")
  public ResponseEntity<TagDTO> createTag(@PathVariable("userId") UUID userId, @RequestBody TagDTO tagDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(tagService.createTag(userId, tagDTO));
  }

  @GetMapping("/{tagId}")
  public ResponseEntity<TagDTO> getTag(@PathVariable("tagId") UUID tagId) {
    return ResponseEntity.ok().body(tagService.findByTagId(tagId));
  }

  @Operation(summary = "Rename tag", description = "Updates the name of an existing tag.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tag renamed", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = TagDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Tag not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PatchMapping("/{tagId}")
  public ResponseEntity<TagDTO> renameTag(@PathVariable("tagId") UUID tagId, @RequestBody
  RenameRequestDTO newName) {
    return ResponseEntity.ok().body(tagService.renameTag(tagId, newName.name()));
  }

  @Operation(summary = "Delete tag", description = "Deletes a tag. If force is true, detaches it from all cards first.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tag deleted", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Tag not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "409", description = "Tag is in use and force is false", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @DeleteMapping("/{tagId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteTag(@PathVariable("tagId") UUID tagId,
      @Parameter(description = "Force delete if tag is in use")
      @RequestParam(name = "force", defaultValue = "false") boolean force) {
    tagService.deleteTag(tagId, force);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Tag deleted successfully."));
  }
}
