package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.service.local.TemplateService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data/templates")
public class TemplateController {

  private final TemplateService templateService;

  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @PostMapping
  public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(templateService.createTemplate(templateDTO));
  }

  @GetMapping("/{templateId}")
  public ResponseEntity<TemplateDTO> getTemplateById(@PathVariable("templateId") UUID templateId) {
    return ResponseEntity.ok().body(templateService.getTemplateById(templateId));
  }

  @DeleteMapping("/{templateId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteTemplate(
      @PathVariable(name = "templateId") UUID templateId) {
    templateService.deleteTemplate(templateId);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Template Successfully deleted"));
  }
}
