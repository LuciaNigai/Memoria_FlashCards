package com.lucia.memoria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fields")
public class Field extends BaseEntity{

  @Column(name = "field_id", nullable = false, unique = true, updatable = false)
  private UUID fieldId = UUID.randomUUID();

  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", referencedColumnName = "id")
  private Card card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_field_id", referencedColumnName = "id")
  private TemplateField templateField;

  // Static Factory Method
  public static Field createNew(Card card, TemplateField templateField, String content) {
    Field field = new Field();
    field.setFieldId(UUID.randomUUID());
    field.setCard(card);
    field.setTemplateField(templateField);
    field.updateContent(content);
    return field;
  }

  public void updateContent(String newContent) {
    this.content = newContent;
  }
}
