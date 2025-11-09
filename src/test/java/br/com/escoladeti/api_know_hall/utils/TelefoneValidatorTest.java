package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.util.TelefoneValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelefoneValidatorTest {

  @InjectMocks
  private TelefoneValidator telefoneValidator;

  @Test
  void isValid_WithValidCelular11Digits_ShouldReturnTrue() {
    assertTrue(telefoneValidator.isValid("11987654321"));
    assertTrue(telefoneValidator.isValid("21987654321"));
    assertTrue(telefoneValidator.isValid("85987654321"));
  }

  @Test
  void isValid_WithValidFixo10Digits_ShouldReturnTrue() {
    assertTrue(telefoneValidator.isValid("1133334444"));
    assertTrue(telefoneValidator.isValid("2133334444"));
    assertTrue(telefoneValidator.isValid("8533334444"));
  }

  @Test
  void isValid_WithFormattedPhone_ShouldReturnTrue() {
    assertTrue(telefoneValidator.isValid("(11) 98765-4321"));
    assertTrue(telefoneValidator.isValid("(11) 3333-4444"));
    assertTrue(telefoneValidator.isValid("11 98765-4321"));
    assertTrue(telefoneValidator.isValid("11-98765-4321"));
  }

  @Test
  void isValid_WithNullTelefone_ShouldReturnTrue() {
    // Telefone é opcional
    assertTrue(telefoneValidator.isValid(null));
  }

  @Test
  void isValid_WithEmptyTelefone_ShouldReturnTrue() {
    // Telefone é opcional
    assertTrue(telefoneValidator.isValid(""));
  }

  @Test
  void isValid_WithTooShortTelefone_ShouldReturnFalse() {
    assertFalse(telefoneValidator.isValid("119876543"));
    assertFalse(telefoneValidator.isValid("11333344"));
    assertFalse(telefoneValidator.isValid("123456"));
  }

  @Test
  void isValid_WithTooLongTelefone_ShouldReturnFalse() {
    assertFalse(telefoneValidator.isValid("119876543210"));
    assertFalse(telefoneValidator.isValid("11987654321012"));
  }

  @Test
  void isValid_WithInvalidDDD_ShouldReturnFalse() {
    assertFalse(telefoneValidator.isValid("00987654321"));
    assertFalse(telefoneValidator.isValid("01987654321"));
    assertFalse(telefoneValidator.isValid("09987654321"));
    assertFalse(telefoneValidator.isValid("10987654321"));
  }

  @Test
  void isValid_With11DigitsButNotStartingWith9_ShouldReturnFalse() {
    assertFalse(telefoneValidator.isValid("11887654321"));
    assertFalse(telefoneValidator.isValid("11787654321"));
    assertFalse(telefoneValidator.isValid("11187654321"));
  }

  @Test
  void isValid_WithOnlyLetters_ShouldReturnFalse() {
    assertFalse(telefoneValidator.isValid("abcdefghijk"));
  }

  @Test
  void formatarTelefone_WithFormattedPhone_ShouldReturnOnlyNumbers() {
    assertEquals("11987654321", telefoneValidator.formatarTelefone("(11) 98765-4321"));
    assertEquals("1133334444", telefoneValidator.formatarTelefone("(11) 3333-4444"));
    assertEquals("11987654321", telefoneValidator.formatarTelefone("11 98765-4321"));
  }

  @Test
  void formatarTelefone_WithUnformattedPhone_ShouldReturnSame() {
    assertEquals("11987654321", telefoneValidator.formatarTelefone("11987654321"));
  }

  @Test
  void formatarTelefone_WithNullPhone_ShouldReturnNull() {
    assertNull(telefoneValidator.formatarTelefone(null));
  }

  @Test
  void formatarTelefone_WithMixedCharacters_ShouldReturnOnlyNumbers() {
    assertEquals("11987654321", telefoneValidator.formatarTelefone("11abc987def654ghi321"));
    assertEquals("11987654321", telefoneValidator.formatarTelefone("11 987 654 321"));
  }

  @Test
  void formatarParaExibicao_WithCelular11Digits_ShouldReturnFormattedCelular() {
    assertEquals("(11) 98765-4321", telefoneValidator.formatarParaExibicao("11987654321"));
    assertEquals("(21) 99876-5432", telefoneValidator.formatarParaExibicao("21998765432"));
  }

  @Test
  void formatarParaExibicao_WithFixo10Digits_ShouldReturnFormattedFixo() {
    assertEquals("(11) 3333-4444", telefoneValidator.formatarParaExibicao("1133334444"));
    assertEquals("(21) 2222-3333", telefoneValidator.formatarParaExibicao("2122223333"));
  }

  @Test
  void formatarParaExibicao_WithNullPhone_ShouldReturnNull() {
    assertNull(telefoneValidator.formatarParaExibicao(null));
  }

  @Test
  void formatarParaExibicao_WithInvalidLength_ShouldReturnOriginal() {
    assertEquals("123456", telefoneValidator.formatarParaExibicao("123456"));
    assertEquals("123456789012", telefoneValidator.formatarParaExibicao("123456789012"));
  }

  @Test
  void formatarParaExibicao_WithAlreadyFormatted_ShouldFormatCorrectly() {
    assertEquals("(11) 98765-4321", telefoneValidator.formatarParaExibicao("(11) 98765-4321"));
  }
}
