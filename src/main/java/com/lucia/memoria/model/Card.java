package com.lucia.memoria.model;

import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "card_id", nullable = false, unique = true, updatable = false)
  private UUID cardId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deck_id", referencedColumnName = "id", nullable = false)
  private Deck deck;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", referencedColumnName = "id", nullable = false)
  private Template template;

  @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
  @OrderBy("templateField ASC")
  private List<Field> fields = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "cards_tags",
      joinColumns = @JoinColumn(name = "card_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id")
  )
  private Set<Tag> tags;

  public Card(Deck deck, Template template) {
    this.cardId = UUID.randomUUID();
    this.deck = deck;
    this.template = template;
  }

  public void addField(Field field) {
    fields.add(field);
    field.setCard(this);
  }

  public void syncFields(List<FieldMinimalDTO> dtos, Map<UUID, TemplateField> templateFields) {
    // 1. Create a lookup map of current fields for easy access
    Map<UUID, Field> existingFields = this.fields.stream()
        .filter(f -> f.getTemplateField() != null)
        .collect(Collectors.toMap(f -> f.getTemplateField().getTemplateFieldId(), f-> f));

    for (FieldMinimalDTO dto : dtos) {
      UUID templateId = dto.getTemplateFieldId();
      Field field = existingFields.get(templateId);

      if(field != null) {
        // It exists? Just update it
        field.updateContent(dto.getContent());
      } else if(templateFields.containsKey(templateId)) {
        // New field? Create it and add to this card
        Field newField = Field.createNew(this, templateFields.get(templateId), dto.getContent());
        this.addField(newField);
      } else {
        throw new ConflictWithDataException("Invalid Field templateId");
      }
    }
  }

  public Optional<Field> getFieldByTemplateId(UUID templateFieldId) {
    return this.fields.stream()
        .filter(f -> f.getTemplateField() != null)
        .filter(f -> f.getTemplateField().getTemplateFieldId().equals(templateFieldId))
        .findFirst();
  }
}
