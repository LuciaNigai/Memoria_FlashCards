package com.lucia.memoria.repository;

import com.lucia.memoria.model.TemplateField;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {

  Optional<TemplateField> findByTemplateFieldId(UUID templateFieldId);
}
