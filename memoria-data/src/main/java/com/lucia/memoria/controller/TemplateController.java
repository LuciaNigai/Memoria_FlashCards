package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.service.local.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Templates", description = "Endpoints for managing card templates and their field definitions")
public class TemplateController {

  private final TemplateService templateService;

  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @Operation(summary = "Create new template", description = "Defines a new card template with specific field types.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created template", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateDTO.class))
      }),
      @ApiResponse(responseCode = "400", description = "Invalid template configuration", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PostMapping
  public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(templateService.createTemplate(templateDTO));
  }

  @Operation(summary = "Get template by id", description = "Returns a template and its field definitions.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found template", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Template not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @GetMapping("/{templateId}")
  public ResponseEntity<TemplateDTO> getTemplateById(@PathVariable("templateId") UUID templateId) {
    return ResponseEntity.ok().body(templateService.getTemplateById(templateId));
  }

  @Operation(summary = "Delete template", description = "Removes a template.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Template deleted", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Template not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "409", description = "Template is in use by existing cards", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @DeleteMapping("/{templateId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteTemplate(
      @PathVariable(name = "templateId") UUID templateId) {
    templateService.deleteTemplate(templateId);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Template Successfully deleted"));
  }
}
