package com.lucia.memoria.model;

import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.TemplateFieldType;
import jakarta.persistence.*;
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
@Table(name = "template_fields")
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
