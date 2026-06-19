package com.lucia.memoria.model;

import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.TemplateFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "template_fields")
public class TemplateField extends BaseEntity{

    @Column(name = "template_field_id", nullable = false, unique = true, updatable = false)
    private UUID templateFieldId = UUID.randomUUID();

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_role", nullable = false)
    private FieldRole fieldRole;

    @Embedded
    private TemplateFieldType templateFieldType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private Template template;
}
