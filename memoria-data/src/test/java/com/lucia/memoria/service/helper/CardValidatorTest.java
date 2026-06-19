package com.lucia.memoria.service.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.repository.CardRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardValidatorTest {

  @Mock
  private CardRepository cardRepository;

  @InjectMocks
  private CardValidator cardValidator;


  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("Should not throw exception when duplicates does not exist.")
  void validateDuplicates_DoesNotHaveDuplicates_existsCleanly(boolean saveDuplicateFlag) {
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setContent("test content");

    doReturn(Collections.emptyList())
        .when(cardRepository)
        .findCardIdsByFieldRoleAndContent(FieldRole.FRONT, fieldRequestDTO.getContent());

    assertDoesNotThrow(() ->
        cardValidator.validateDuplicates(fieldRequestDTO, saveDuplicateFlag, null,
            FieldRole.FRONT));
  }

  @Test
  @DisplayName("Should not throw exception when duplicate exists but saveDuplicate is true")
  void validateDuplicates_hasDuplicatesButAllowed_exitsCleanly() {
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setContent("test content");

    doReturn(List.of(UUID.randomUUID()))
        .when(cardRepository)
        .findCardIdsByFieldRoleAndContent(FieldRole.FRONT, fieldRequestDTO.getContent());

    assertDoesNotThrow(() ->
        cardValidator.validateDuplicates(fieldRequestDTO, true, null, FieldRole.FRONT));
  }

  @Test
  @DisplayName("Should throw an exception when duplicate exists and saveDuplicate is false")
  void validateDuplicates_hasDuplicatesAndNotAllowed_throwsException() {
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setContent("test content");

    doReturn(List.of(UUID.randomUUID()))
        .when(cardRepository)
        .findCardIdsByFieldRoleAndContent(FieldRole.FRONT, fieldRequestDTO.getContent());

    assertThrows(DuplicateException.class,
        () -> cardValidator.validateDuplicates(fieldRequestDTO, false, null, FieldRole.FRONT));
  }

  @Test
  @DisplayName("Should not throw an exception for card with at least one FRONT and one BACK field.")
   void validateCardStructure_existsCleanly() {
    Card card = createCardWithRolesAndContent(
        new RoleContent(FieldRole.FRONT, "Valid content"),
        new RoleContent(FieldRole.BACK, "Valid content")
    );

    assertDoesNotThrow(() ->
        cardValidator.validateCardStructure(card));
   }

   @Test
   @DisplayName("Should throw ConflictWithDataException when FRONT field content is blank")
   void validateCardStructure_BlankFrontField_throwsException() {
     Card card = createCardWithRolesAndContent(
         new RoleContent(FieldRole.FRONT, " "),
         new RoleContent(FieldRole.BACK, "Valid content")
     );

     assertThrows(ConflictWithDataException.class, () ->
         cardValidator.validateCardStructure(card)
     );
   }

   @Test
   @DisplayName("Should throw ConflictWithDataExtension when BACK field is null")
   void validateCardStructure_nullBackField_throwsException() {
     Card card = createCardWithRolesAndContent(
         new RoleContent(FieldRole.FRONT, "Valid content"),
         new RoleContent(FieldRole.BACK, null)
     );

     assertThrows(ConflictWithDataException.class, () ->
         cardValidator.validateCardStructure(card)
     );
   }

   @Test
   @DisplayName("Should throw ConflictWithDataException when fields are missing")
   void validateCardStructure_missingFields_throwsException(){
     Card card = new Card();

     assertThrows(ConflictWithDataException.class, () ->
         cardValidator.validateCardStructure(card)
     );
   }


  private record RoleContent(FieldRole role, String content) {}

  /**
   * Helper method to generate a Card containing fields with specified roles.
   */
  private Card createCardWithRolesAndContent(RoleContent... items) {
    List<Field> fields = Arrays.stream(items)
        .map(item -> {
          TemplateField tf = new TemplateField();
          tf.setFieldRole(item.role());

          Field field = new Field();
          field.setTemplateField(tf);
          field.setContent(item.content()); // 🚀 Map the text data dynamically
          return field;
        })
        .toList();

    Card card = new Card();
    card.setFields(fields);
    return card;
  }
}