package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.RenameRequestDTO;
import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.service.local.TagService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
public class TagController {

  private final TagService tagService;

  @PostMapping
  public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
    TagDTO created = tagService.createTag(tagDTO.userId(), tagDTO.name());
    URI location = URI.create("/tags/" + created.tagId());
    return ResponseEntity.created(location).body(created);
  }

//  TODO change ti to getUserTags and move to user Controller
  @GetMapping
  public ResponseEntity<List<TagDTO>> getTags() {
    return ResponseEntity.ok().body(tagService.getAllTags());
  }

  @PatchMapping("/{tagId}")
  public ResponseEntity<TagDTO> renameTag(@PathVariable("tagId") UUID tagId, @RequestBody
      RenameRequestDTO newName) {
    return ResponseEntity.ok().body(tagService.renameTag(tagId, newName.name()));
  }

  @DeleteMapping("/{tagId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteTag(@PathVariable("tagId") UUID tagId,
      @RequestParam(name = "force", defaultValue = "false") boolean force) {
    tagService.deleteTag(tagId, force);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Tag deleted successfully."));
  }
}
