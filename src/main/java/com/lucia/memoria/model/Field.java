package com.lucia.memoria.model;

import com.lucia.memoria.helper.FieldType;
import com.lucia.memoria.helper.TemplateFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "fields")
public class Field {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

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
    field.updateContent(content); // Reuse your validation logic!
    return field;
  }

  public void updateContent(String newContent) {
    TemplateFieldType type = this.templateField.getTemplateFieldType();

    if (type == null) {
      throw new IllegalArgumentException("TemplateFieldType must not be null");
    }

    // 1. Handle Enum/Multi-tag logic internally
    if (type.getFieldType() == FieldType.ENUM || type.getFieldType() == FieldType.MULTI_TAG) {
      validateEnumOptions(newContent, type);
    }

    // 2. Set the content
    this.content = newContent;
  }
  private void validateEnumOptions(String content, TemplateFieldType type) {
    if (type.getOptions() == null || !type.getOptions().contains(content)) {
      throw new IllegalArgumentException(
          "Invalid option. Choose one of: " + type.getOptions()
      );
    }
  }

}
