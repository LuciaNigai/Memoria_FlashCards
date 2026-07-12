package com.lucia.memoria.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucia.memoria.controller.TemplateController;
import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateRequestDTO;
import com.lucia.memoria.dto.local.TemplateResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.service.local.TemplateService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TemplateController.class)
class TemplateControllerWebSliceTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @MockitoBean
  TemplateService templateService;

  @Test
  @DisplayName("POST /api/data/templates - Should create template and return 200 OK")
  void createTemplate_shouldReturn200() throws Exception {
    UUID templateId = UUID.randomUUID();
    String templateName = "Language";
    TemplateFieldRequestDTO templateFieldRequestDTO = new TemplateFieldRequestDTO();
    templateFieldRequestDTO.setName("Word");
    templateFieldRequestDTO.setFieldRole(FieldRole.FRONT);
    TemplateFieldRequestDTO secondTemplateFieldRequestDTO = new TemplateFieldRequestDTO();
    secondTemplateFieldRequestDTO.setName("Translation");
    secondTemplateFieldRequestDTO.setFieldRole(FieldRole.BACK);

    TemplateRequestDTO templateRequestDTO = new TemplateRequestDTO();
    templateRequestDTO.setOwnerId(UUID.randomUUID());
    templateRequestDTO.setName(templateName);
    templateRequestDTO.setFields(List.of(templateFieldRequestDTO, secondTemplateFieldRequestDTO));

    TemplateResponseDTO templateResponseDTO = new TemplateResponseDTO();
    templateResponseDTO.setId(templateId);
    templateResponseDTO.setName(templateName);

    when(templateService.createTemplate(any(TemplateRequestDTO.class))).thenReturn(templateResponseDTO);

    mockMvc.perform(post("/api/data/templates")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(templateRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(templateId.toString()))
        .andExpect(jsonPath("$.name").value(templateName));
    verify(templateService).createTemplate(any(TemplateRequestDTO.class));
  }

  @Test
  @DisplayName("POST /api/data/templates - Should return 400 if template with such name already exists")
  void createTemplate_templateAlreadyExists_shouldReturn400() throws Exception {
    String templateName = "Language";
    TemplateFieldRequestDTO templateFieldRequestDTO = new TemplateFieldRequestDTO();
    templateFieldRequestDTO.setName("Word");
    templateFieldRequestDTO.setFieldRole(FieldRole.FRONT);
    TemplateFieldRequestDTO secondTemplateFieldRequestDTO = new TemplateFieldRequestDTO();
    secondTemplateFieldRequestDTO.setName("Translation");
    secondTemplateFieldRequestDTO.setFieldRole(FieldRole.BACK);

    TemplateRequestDTO templateRequestDTO = new TemplateRequestDTO();
    templateRequestDTO.setOwnerId(UUID.randomUUID());
    templateRequestDTO.setName(templateName);
    templateRequestDTO.setFields(List.of(templateFieldRequestDTO, secondTemplateFieldRequestDTO));
    String errorMessage = "Template with name " + templateRequestDTO.getName() + " already exists";

    when(templateService.createTemplate(any(TemplateRequestDTO.class))).thenThrow(new IllegalArgumentException(errorMessage));

    mockMvc.perform(post("/api/data/templates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(templateRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(templateService).createTemplate(any(TemplateRequestDTO.class));
  }

  @Test
  @DisplayName("POST /api/data/templates - Should return 400 if template with such name already exists")
  void createTemplate_missingTemplateName_shouldReturn400() throws Exception {
    TemplateFieldRequestDTO templateFieldRequestDTO = new TemplateFieldRequestDTO();
    templateFieldRequestDTO.setName("Word");
    templateFieldRequestDTO.setFieldRole(FieldRole.FRONT);
    TemplateFieldRequestDTO secondTemplateFieldRequestDTO = new TemplateFieldRequestDTO();
    secondTemplateFieldRequestDTO.setName("Translation");
    secondTemplateFieldRequestDTO.setFieldRole(FieldRole.BACK);

    TemplateRequestDTO templateRequestDTO = new TemplateRequestDTO();
    templateRequestDTO.setOwnerId(UUID.randomUUID());
    templateRequestDTO.setFields(List.of(templateFieldRequestDTO, secondTemplateFieldRequestDTO));

    mockMvc.perform(post("/api/data/templates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(templateRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]").value("name cannot be empty"));
  }

  @Test
  @DisplayName("GET /api/data/templates/{templateId} - Should return template and 200 OK")
  void getTemplateById_shouldReturn200() throws Exception {
    UUID templateId = UUID.randomUUID();
    String templateName = "Language";
    TemplateResponseDTO templateResponseDTO = new TemplateResponseDTO();
    templateResponseDTO.setId(templateId);
    templateResponseDTO.setName(templateName);

    when(templateService.getTemplateById(templateId)).thenReturn(templateResponseDTO);

    mockMvc.perform(get("/api/data/templates/{templateId}", templateId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(templateId.toString()))
        .andExpect(jsonPath("$.name").value(templateName));
    verify(templateService).getTemplateById(templateId);
  }

  @Test
  @DisplayName("GET /api/data/templates/{templateId} - Should throw 404 when template is not found")
  void getTemplateById_templateNotFound_shouldReturn404() throws Exception {
    UUID templateId = UUID.randomUUID();

    String errorMessage = "Template Not found";

    when(templateService.getTemplateById(templateId)).thenThrow(new NotFoundException(errorMessage));

    mockMvc.perform(get("/api/data/templates/{templateId}", templateId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(templateService).getTemplateById(templateId);
  }

  @Test
  @DisplayName("DELETE /api/data/templates/{templateId} - Should delete template and return 200 OK")
  void deleteTemplate_shouldReturn200() throws Exception {
    // Given
    UUID templateId = UUID.randomUUID();
    String successMessage = "Template Successfully deleted";

    doNothing().when(templateService).deleteTemplate(templateId);

    // When & Then
    mockMvc.perform(delete("/api/data/templates/{templateId}", templateId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(successMessage));

    verify(templateService).deleteTemplate(templateId);
  }

  @Test
  @DisplayName("DELETE /api/data/templates/{templateId} - Should return 404 when template not found")
  void deleteTemplate_templateNotFound_shouldReturn404() throws Exception {
    // Given
    UUID templateId = UUID.randomUUID();
    String errorMessage = "Template you are trying to delete does not exist";

    doThrow(new NotFoundException(errorMessage))
        .when(templateService).deleteTemplate(templateId);

    // When & Then
    mockMvc.perform(delete("/api/data/templates/{templateId}", templateId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));

    verify(templateService).deleteTemplate(templateId);
  }

  @Test
  @DisplayName("DELETE /api/data/templates/{templateId} - Should return 409 when template is in use")
  void deleteTemplate_templateInUse_shouldReturn409() throws Exception {
    // Given
    UUID templateId = UUID.randomUUID();
    String errorMessage = "Template cannot be deleted. There are still cards that use that template.";

    doThrow(new ConflictWithDataException(errorMessage))
        .when(templateService).deleteTemplate(templateId);

    // When & Then
    mockMvc.perform(delete("/api/data/templates/{templateId}", templateId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(errorMessage));

    verify(templateService).deleteTemplate(templateId);
  }

}
