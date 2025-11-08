package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.util.EmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

  @InjectMocks
  private EmailValidator emailValidator;

  @Test
  void isValid_WithValidEmail_ShouldReturnTrue() {
    assertTrue(emailValidator.isValid("test@example.com"));
    assertTrue(emailValidator.isValid("user.name@example.com"));
    assertTrue(emailValidator.isValid("user+tag@example.co.uk"));
    assertTrue(emailValidator.isValid("user_name@example-domain.com"));
    assertTrue(emailValidator.isValid("user123@test123.com"));
  }

  @Test
  void isValid_WithNullEmail_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid(null));
  }

  @Test
  void isValid_WithEmptyEmail_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid(""));
  }

  @Test
  void isValid_WithEmailWithoutAt_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("testexample.com"));
    assertFalse(emailValidator.isValid("test.example.com"));
  }

  @Test
  void isValid_WithEmailWithoutDomain_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test@"));
    assertFalse(emailValidator.isValid("test@.com"));
  }

  @Test
  void isValid_WithEmailWithoutLocalPart_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("@example.com"));
  }

  @Test
  void isValid_WithEmailWithoutTLD_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test@example"));
  }

  @Test
  void isValid_WithMultipleAtSymbols_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test@@example.com"));
    assertFalse(emailValidator.isValid("test@test@example.com"));
  }

  @Test
  void isValid_WithSpaces_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test @example.com"));
    assertFalse(emailValidator.isValid("test@ example.com"));
    assertFalse(emailValidator.isValid("test @example .com"));
  }

  @Test
  void isValid_WithInvalidCharacters_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test#@example.com"));
    assertFalse(emailValidator.isValid("test$@example.com"));
    assertFalse(emailValidator.isValid("test%@example.com"));
  }

  @Test
  void isValid_WithTooShortEmail_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("a@b.c"));
  }

  @Test
  void isValid_WithTooLongEmail_ShouldReturnFalse() {
    String longEmail = "a".repeat(250) + "@example.com";
    assertFalse(emailValidator.isValid(longEmail));
  }

  @Test
  void isValid_WithEmailWithWhitespace_ShouldReturnTrue() {
    assertTrue(emailValidator.isValid("  test@example.com"));
    assertTrue(emailValidator.isValid("test@example.com  "));
    assertTrue(emailValidator.isValid("  test@example.com  "));
  }

  @Test
  void isValid_WithSpacesInMiddle_ShouldReturnFalse() {
    assertFalse(emailValidator.isValid("test @example.com"));
    assertFalse(emailValidator.isValid("test@ example.com"));
    assertFalse(emailValidator.isValid("test @example .com"));
  }

  @Test
  void normalizar_WithValidEmail_ShouldReturnLowercaseAndTrimmed() {
    assertEquals("test@example.com", emailValidator.normalizar("TEST@EXAMPLE.COM"));
    assertEquals("test@example.com", emailValidator.normalizar("  test@example.com  "));
    assertEquals("user.name@example.com", emailValidator.normalizar("User.Name@Example.Com"));
  }

  @Test
  void normalizar_WithNullEmail_ShouldReturnNull() {
    assertNull(emailValidator.normalizar(null));
  }

  @Test
  void normalizar_WithEmptyEmail_ShouldReturnEmpty() {
    assertEquals("", emailValidator.normalizar(""));
    assertEquals("", emailValidator.normalizar("   "));
  }
}
