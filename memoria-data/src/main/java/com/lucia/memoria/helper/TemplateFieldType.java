package com.lucia.memoria.helper;

import jakarta.persistence.*;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
@ToString
public class TemplateFieldType {
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @ElementCollection
    @CollectionTable(
            name = "template_field_options",
            joinColumns = @JoinColumn(name = "template_field_id")
    )
    @Column(name = "option_value")
    private List<String> options;

    public TemplateFieldType(FieldType fieldType) {
        this(fieldType, List.of());
    }

    public TemplateFieldType(FieldType fieldType, List<String> options) {
        this.fieldType = fieldType;
        this.options = options;
    }
}
