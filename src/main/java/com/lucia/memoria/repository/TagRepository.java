package com.lucia.memoria.repository;

import com.lucia.memoria.model.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String name);

  Optional<Tag> findByTagId(UUID tagId);
}
