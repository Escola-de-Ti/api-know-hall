package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.util.CpfValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CpfValidatorTest {

  @InjectMocks
  private CpfValidator cpfValidator;

  @Test
  void isValid_WithValidCPF_ShouldReturnTrue() {
    assertTrue(cpfValidator.isValid("12345678909"));
    assertTrue(cpfValidator.isValid("11144477735"));
    assertTrue(cpfValidator.isValid("52998224725"));
    assertTrue(cpfValidator.isValid("40442820135"));
  }

  @Test
  void isValid_WithNullCPF_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid(null));
  }

  @Test
  void isValid_WithEmptyCPF_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid(""));
  }

  @Test
  void isValid_WithCPFShorterThan11_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid("123456789"));
    assertFalse(cpfValidator.isValid("12345"));
  }

  @Test
  void isValid_WithCPFLongerThan11_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid("123456789012"));
    assertFalse(cpfValidator.isValid("1234567890123"));
  }

  @Test
  void isValid_WithNonNumericCPF_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid("123.456.789-09"));
    assertFalse(cpfValidator.isValid("12345678ABC"));
    assertFalse(cpfValidator.isValid("abcdefghijk"));
  }

  @Test
  void isValid_WithAllSameDigits_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid("00000000000"));
    assertFalse(cpfValidator.isValid("11111111111"));
    assertFalse(cpfValidator.isValid("22222222222"));
    assertFalse(cpfValidator.isValid("33333333333"));
    assertFalse(cpfValidator.isValid("44444444444"));
    assertFalse(cpfValidator.isValid("55555555555"));
    assertFalse(cpfValidator.isValid("66666666666"));
    assertFalse(cpfValidator.isValid("77777777777"));
    assertFalse(cpfValidator.isValid("88888888888"));
    assertFalse(cpfValidator.isValid("99999999999"));
  }

  @Test
  void isValid_WithInvalidCheckDigits_ShouldReturnFalse() {
    assertFalse(cpfValidator.isValid("12345678901"));
    assertFalse(cpfValidator.isValid("12345678902"));
    assertFalse(cpfValidator.isValid("98765432199"));
    assertFalse(cpfValidator.isValid("11144477700"));
  }

  @Test
  void formatarCpf_WithFormattedCPF_ShouldReturnOnlyNumbers() {
    assertEquals("12345678909", cpfValidator.formatarCpf("123.456.789-09"));
    assertEquals("11144477735", cpfValidator.formatarCpf("111.444.777-35"));
  }

  @Test
  void formatarCpf_WithUnformattedCPF_ShouldReturnSame() {
    assertEquals("12345678909", cpfValidator.formatarCpf("12345678909"));
  }

  @Test
  void formatarCpf_WithNullCPF_ShouldReturnNull() {
    assertNull(cpfValidator.formatarCpf(null));
  }

  @Test
  void formatarCpf_WithMixedCharacters_ShouldReturnOnlyNumbers() {
    assertEquals("12345678909", cpfValidator.formatarCpf("123abc456def789-09"));
    assertEquals("12345678909", cpfValidator.formatarCpf("123 456 789 09"));
  }
}
