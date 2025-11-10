package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.util.SenhaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SenhaValidatorTest {

  @InjectMocks
  private SenhaValidator senhaValidator;

  @Test
  void isValid_WithValidPassword_ShouldReturnTrue() {
    assertTrue(senhaValidator.isValid("Senha@123"));
    assertTrue(senhaValidator.isValid("Pass@word1"));
    assertTrue(senhaValidator.isValid("MyP@ssw0rd"));
    assertTrue(senhaValidator.isValid("Test_123!"));
    assertTrue(senhaValidator.isValid("Abcd1234!"));
  }

  @Test
  void isValid_WithNullPassword_ShouldReturnFalse() {
    assertFalse(senhaValidator.isValid(null));
  }

  @Test
  void isValid_WithEmptyPassword_ShouldReturnFalse() {
    assertFalse(senhaValidator.isValid(""));
  }

  @Test
  void isValid_WithTooShortPassword_ShouldReturnFalse() {
    assertFalse(senhaValidator.isValid("Ab@1"));
    assertFalse(senhaValidator.isValid("Test@1"));
    assertFalse(senhaValidator.isValid("Pass@12"));
  }

  @Test
  void isValid_WithoutNumber_ShouldReturnFalse() {
    assertFalse(senhaValidator.isValid("Password@"));
    assertFalse(senhaValidator.isValid("SenhaForte!"));
    assertFalse(senhaValidator.isValid("Test@Test"));
  }

  @Test
  void isValid_WithoutSpecialCharacter_ShouldReturnFalse() {
    assertFalse(senhaValidator.isValid("Password123"));
    assertFalse(senhaValidator.isValid("SenhaForte1"));
    assertFalse(senhaValidator.isValid("Test1234"));
  }

  @Test
  void isValid_WithAllSpecialCharacters_ShouldReturnTrue() {
    assertTrue(senhaValidator.isValid("Pass!word1"));
    assertTrue(senhaValidator.isValid("Pass@word1"));
    assertTrue(senhaValidator.isValid("Pass#word1"));
    assertTrue(senhaValidator.isValid("Pass$word1"));
    assertTrue(senhaValidator.isValid("Pass%word1"));
    assertTrue(senhaValidator.isValid("Pass^word1"));
    assertTrue(senhaValidator.isValid("Pass&word1"));
    assertTrue(senhaValidator.isValid("Pass*word1"));
  }

  @Test
  void getErrosValidacao_WithNullPassword_ShouldReturnSingleError() {
    List<String> erros = senhaValidator.getErrosValidacao(null);
    assertEquals(1, erros.size());
    assertTrue(erros.get(0).contains("obrigatória"));
  }

  @Test
  void getErrosValidacao_WithShortPassword_ShouldReturnLengthError() {
    List<String> erros = senhaValidator.getErrosValidacao("Ab@1");
    assertTrue(erros.stream().anyMatch(e -> e.contains("mínimo 8 caracteres")));
  }

  @Test
  void getErrosValidacao_WithoutNumber_ShouldReturnNumberError() {
    List<String> erros = senhaValidator.getErrosValidacao("Password@");
    assertTrue(erros.stream().anyMatch(e -> e.contains("pelo menos um número")));
  }

  @Test
  void getErrosValidacao_WithoutSpecialChar_ShouldReturnSpecialCharError() {
    List<String> erros = senhaValidator.getErrosValidacao("Password123");
    assertTrue(erros.stream().anyMatch(e -> e.contains("caractere especial")));
  }

  @Test
  void getErrosValidacao_WithMultipleErrors_ShouldReturnAllErrors() {
    List<String> erros = senhaValidator.getErrosValidacao("abc");
    assertTrue(erros.size() >= 3);
    assertTrue(erros.stream().anyMatch(e -> e.contains("mínimo 8 caracteres")));
    assertTrue(erros.stream().anyMatch(e -> e.contains("pelo menos um número")));
    assertTrue(erros.stream().anyMatch(e -> e.contains("caractere especial")));
  }

  @Test
  void getErrosValidacao_WithValidPassword_ShouldReturnEmptyList() {
    List<String> erros = senhaValidator.getErrosValidacao("Senha@123");
    assertTrue(erros.isEmpty());
  }

  @Test
  void getMensagemErro_WithValidPassword_ShouldReturnNull() {
    assertNull(senhaValidator.getMensagemErro("Senha@123"));
  }

  @Test
  void getMensagemErro_WithInvalidPassword_ShouldReturnCombinedMessage() {
    String mensagem = senhaValidator.getMensagemErro("abc");
    assertNotNull(mensagem);
    assertTrue(mensagem.contains("mínimo 8 caracteres"));
    assertTrue(mensagem.contains("pelo menos um número"));
    assertTrue(mensagem.contains("caractere especial"));
  }

  @Test
  void calcularForcaSenha_WithVeryWeakPassword_ShouldReturnLowScore() {
    assertEquals(0, senhaValidator.calcularForcaSenha(""));
    assertEquals(0, senhaValidator.calcularForcaSenha(null));
    assertTrue(senhaValidator.calcularForcaSenha("abc") <= 1);
  }

  @Test
  void calcularForcaSenha_WithWeakPassword_ShouldReturnLowScore() {
    int forca = senhaValidator.calcularForcaSenha("password123");
    assertTrue(forca >= 2 && forca <= 3);
  }

  @Test
  void calcularForcaSenha_WithStrongPassword_ShouldReturnHighScore() {
    assertEquals(4, senhaValidator.calcularForcaSenha("Senh@Forte123"));
    assertEquals(4, senhaValidator.calcularForcaSenha("MyP@ssw0rd!Test"));
  }

  @Test
  void getDescricaoForca_WithDifferentPasswords_ShouldReturnCorrectDescription() {
    assertEquals("Muito fraca", senhaValidator.getDescricaoForca("abc"));
    assertEquals("Média", senhaValidator.getDescricaoForca("password123"));
    assertEquals("Forte", senhaValidator.getDescricaoForca("Senh@Forte123"));
  }

  @Test
  void isValid_WithExactly8Characters_ShouldReturnTrue() {
    assertTrue(senhaValidator.isValid("Senha@12"));
  }

  @Test
  void isValid_WithVeryLongPassword_ShouldReturnTrue() {
    String longPassword = "A1@" + "a".repeat(97);
    assertTrue(senhaValidator.isValid(longPassword));
  }
}
