package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.mapper.TagMapper;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Tag;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class TagService {

  private final TagRepository tagRepository;
  private final TagMapper tagMapper;
  private final CardRepository cardRepository;
  private final UserService userService;

  @Transactional
  public TagDTO createTag(UUID userId, String name) {
    User user = userService.getUserEntityById(userId);
    String normalizedName = name.trim();
    checkForDuplicates(normalizedName);

    Tag tag = new Tag();
    tag.setTagId(UUID.randomUUID());
    tag.setUser(user);
    tag.setName(normalizedName);

    return tagMapper.toDTO(tagRepository.save(tag));
  }

  @Transactional(readOnly = true)
  public List<TagDTO> getAllTags() {
    List<Tag> tags = tagRepository.findAll(Sort.by("name"));
    return tagMapper.toDTOList(tags);
  }

  @Transactional
  public void deleteTag(UUID tagId, boolean force) {
    Tag tag = tagRepository.findByTagId(tagId).orElseThrow(() -> new NotFoundException("Tag you are trying to delete does not exist"));
    Set<Card> cards = tag.getCards();
    if(!cards.isEmpty())
    {
      if(force) {
        cards.forEach(card -> card.getTags().remove(tag));
      }
      else throw new ConflictWithDataException("There are still cards with this tag! Are you sure you want to delete it?", tag);
    }

    tagRepository.delete(tag);
  }

  @Transactional
  public TagDTO renameTag(UUID tagId, String name) {
    Tag tag = tagRepository.findByTagId(tagId).orElseThrow(() -> new NotFoundException("Tag you are trying to rename does not exist"));
    String normalizedName = name.trim();
    if (!tag.getName().equalsIgnoreCase(normalizedName)) {
      checkForDuplicates(normalizedName);
    } else {
      return tagMapper.toDTO(tag);
    }
    tag.setName(normalizedName);
    return tagMapper.toDTO(tagRepository.save(tag));
  }

  private void checkForDuplicates(String name) {
    Optional<Tag> duplicate = tagRepository.findByName(name);
    if (duplicate.isPresent()) {
      log.warn("Attempt to create duplicate tag: {}", name);
      throw new DuplicateException("The tag with that name already exists", name);
    }
  }
}
