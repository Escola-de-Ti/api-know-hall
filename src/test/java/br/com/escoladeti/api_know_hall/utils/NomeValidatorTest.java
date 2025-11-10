package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.util.NomeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NomeValidatorTest {

  @InjectMocks
  private NomeValidator nomeValidator;

  @Test
  void isValid_WithValidName_ShouldReturnTrue() {
    assertTrue(nomeValidator.isValid("João Silva"));
    assertTrue(nomeValidator.isValid("Maria"));
    assertTrue(nomeValidator.isValid("José Carlos"));
    assertTrue(nomeValidator.isValid("Ana Maria da Silva"));
  }

  @Test
  void isValid_WithAccentedCharacters_ShouldReturnTrue() {
    assertTrue(nomeValidator.isValid("José"));
    assertTrue(nomeValidator.isValid("María"));
    assertTrue(nomeValidator.isValid("François"));
    assertTrue(nomeValidator.isValid("Ângela"));
  }

  @Test
  void isValid_WithHyphenAndApostrophe_ShouldReturnTrue() {
    assertTrue(nomeValidator.isValid("Mary-Jane"));
    assertTrue(nomeValidator.isValid("O'Brien"));
    assertTrue(nomeValidator.isValid("Jean-Paul"));
  }

  @Test
  void isValid_WithNullName_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid(null));
  }

  @Test
  void isValid_WithEmptyName_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid(""));
    assertFalse(nomeValidator.isValid("   "));
  }

  @Test
  void isValid_WithTooShortName_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("A"));
    assertFalse(nomeValidator.isValid("J"));
  }

  @Test
  void isValid_WithTooLongName_ShouldReturnFalse() {
    String longName = "a".repeat(101);
    assertFalse(nomeValidator.isValid(longName));
  }

  @Test
  void isValid_WithNumbers_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("João123"));
    assertFalse(nomeValidator.isValid("Maria Silva 456"));
    assertFalse(nomeValidator.isValid("123 Silva"));
  }

  @Test
  void isValid_WithSpecialCharacters_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("João@Silva"));
    assertFalse(nomeValidator.isValid("Maria#Silva"));
    assertFalse(nomeValidator.isValid("José$Carlos"));
    assertFalse(nomeValidator.isValid("Ana&Maria"));
  }

  @Test
  void isValid_WithOnlySpaces_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("     "));
  }

  @Test
  void isValid_WithOnlySpecialCharacters_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("---"));
    assertFalse(nomeValidator.isValid("'''"));
    assertFalse(nomeValidator.isValid("- -"));
  }

  @Test
  void isValid_WithMultipleSpaces_ShouldReturnFalse() {
    assertFalse(nomeValidator.isValid("João  Silva"));
    assertFalse(nomeValidator.isValid("Maria   Santos"));
  }

  @Test
  void isValid_WithLeadingOrTrailingSpaces_ShouldReturnTrue() {
    // O validator faz trim, então deve aceitar
    assertTrue(nomeValidator.isValid("  João Silva  "));
  }

  @Test
  void normalizar_WithValidName_ShouldReturnCapitalized() {
    assertEquals("João Silva", nomeValidator.normalizar("joão silva"));
    assertEquals("Maria Santos", nomeValidator.normalizar("MARIA SANTOS"));
    assertEquals("José Carlos", nomeValidator.normalizar("josé CARLOS"));
  }

  @Test
  void normalizar_WithExtraSpaces_ShouldReturnNormalized() {
    assertEquals("João Silva", nomeValidator.normalizar("  joão  silva  "));
    assertEquals("Maria Santos", nomeValidator.normalizar("maria   santos"));
  }

  @Test
  void normalizar_WithNullName_ShouldReturnNull() {
    assertNull(nomeValidator.normalizar(null));
  }

  @Test
  void normalizar_WithSingleWord_ShouldReturnCapitalized() {
    assertEquals("João", nomeValidator.normalizar("joão"));
    assertEquals("Maria", nomeValidator.normalizar("MARIA"));
  }
}
