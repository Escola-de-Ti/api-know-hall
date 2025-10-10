package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do serviço de palavras proibidas")
class PalavrasProibidasServiceTest {

  private PalavrasProibidasService service;

  @BeforeEach
  void setUp() {
    service = new PalavrasProibidasService();
  }

  @Test
  @DisplayName("Deve detectar palavra proibida exata em diferentes casos")
  void deveDetectarPalavraProibidaExata() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("idiota"));
    assertTrue(service.contemPalavraProibida("IDIOTA"));
    assertTrue(service.contemPalavraProibida("IdIoTa"));
  }

  @Test
  @DisplayName("Deve detectar palavra com acentos")
  void deveDetectarPalavraComAcentos() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("idióta"));
    assertTrue(service.contemPalavraProibida("pôrno"));
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'idiota'")
  @ValueSource(strings = {"1d10t4", "!d!0t@", "i.d.i.o.t.a", "i_d_i_o_t_a"})
  void deveDetectarVariacoesDeIdiota(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'porno'")
  @ValueSource(strings = {"p0rn0", "p@rn0", "p.o.r.n.o", "p_o_r_n"})
  void deveDetectarVariacoesDePorno(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'viagra'")
  @ValueSource(strings = {"v1agr4", "v!agr@", "v.i.a.g.r.a"})
  void deveDetectarVariacoesDeViagra(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'cassino'")
  @ValueSource(strings = {"c@$$!n0", "c4ss1n0", "cassino"})
  void deveDetectarVariacoesDeCassino(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'sexo'")
  @ValueSource(strings = {"s3x0", "s.e.x.o", "sexo"})
  void deveDetectarVariacoesDeSexo(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'puta'")
  @ValueSource(strings = {"put@", "p.u.t.a", "puta"})
  void deveDetectarVariacoesDePuta(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'merda'")
  @ValueSource(strings = {"m3rd@", "m.e.r.d.a", "merda"})
  void deveDetectarVariacoesDeMerda(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @ParameterizedTest
  @DisplayName("Deve detectar variações de 'FDP'")
  @ValueSource(strings = {"f.d.p", "f_d_p", "fdp", "FDP"})
  void deveDetectarVariacoesDeFDP(String palavra) {
    // Act & Assert
    assertTrue(service.contemPalavraProibida(palavra),
      "Deveria detectar a variação: " + palavra);
  }

  @Test
  @DisplayName("Deve detectar repetição excessiva de caracteres")
  void deveDetectarRepeticaoExcessiva() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("aaaaaaa"));
    assertTrue(service.contemPalavraProibida("!!!!!!!"));
    assertTrue(service.contemPalavraProibida("@@@@@@@"));
  }

  @Test
  @DisplayName("Deve detectar muitos números substituindo letras")
  void deveDetectarMuitosNumeros() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("test123456"));
    assertTrue(service.contemPalavraProibida("abc1234"));
  }

  @Test
  @DisplayName("Deve detectar URLs suspeitas")
  void deveDetectarURLsSuspeitas() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("bit.ly/teste"));
    assertTrue(service.contemPalavraProibida("tinyurl.com/abc"));
    assertTrue(service.contemPalavraProibida("goo.gl/xyz"));
  }

  @Test
  @DisplayName("Deve detectar múltiplos símbolos especiais")
  void deveDetectarMultiplosSimbolosEspeciais() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("!!!@@##"));
    assertTrue(service.contemPalavraProibida("$$$%%%"));
  }

  @ParameterizedTest
  @DisplayName("Não deve detectar palavras normais")
  @ValueSource(strings = {"java", "python", "javascript", "programacao", "tecnologia", "desenvolvimento"})
  void naoDeveDetectarPalavrasNormais(String palavra) {
    // Act & Assert
    assertFalse(service.contemPalavraProibida(palavra),
      "Não deveria detectar palavra normal: " + palavra);
  }

  @Test
  @DisplayName("Deve detectar palavra proibida em texto maior")
  void deveDetectarPalavraEmTextoMaior() {
    // Act & Assert
    assertTrue(service.contemPalavraProibida("voce eh um idiota"));
    assertTrue(service.contemPalavraProibida("site de p0rno gratis"));
  }

  @ParameterizedTest
  @DisplayName("Deve retornar false para texto vazio, nulo ou em branco")
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void deveRetornarFalseParaTextoInvalido(String texto) {
    // Act & Assert
    assertFalse(service.contemPalavraProibida(texto));
  }

  @Test
  @DisplayName("Deve identificar a palavra proibida encontrada")
  void deveIdentificarPalavraProibidaEncontrada() {
    // Act & Assert
    assertEquals("IDIOTA", service.identificarPalavraProibida("idiota"));
    assertEquals("PORNO", service.identificarPalavraProibida("porno"));
    assertNotNull(service.identificarPalavraProibida("p0rn0"));
  }

  @Test
  @DisplayName("Deve retornar null quando não encontrar palavra proibida")
  void deveRetornarNullQuandoNaoEncontrarPalavraProibida() {
    // Act & Assert
    assertNull(service.identificarPalavraProibida("java"));
    assertNull(service.identificarPalavraProibida("programacao"));
    assertNull(service.identificarPalavraProibida(""));
  }

  @Test
  @DisplayName("Deve detectar todas as palavras de baixo calão")
  void deveDetectarTodasPalavroesComuns() {
    // Arrange
    String[] palavroes = {"merda", "porra", "caralho", "buceta", "puta", "viado"};

    // Act & Assert
    for (String palavrao : palavroes) {
      assertTrue(service.contemPalavraProibida(palavrao),
        "Deveria detectar: " + palavrao);
    }
  }

  @Test
  @DisplayName("Deve detectar termos discriminatórios")
  void deveDetectarDiscriminacao() {
    // Arrange
    String[] palavras = {"nazista", "hitler", "racista", "homofobia", "xenofobia", "fascista"};

    // Act & Assert
    for (String palavra : palavras) {
      assertTrue(service.contemPalavraProibida(palavra),
        "Deveria detectar: " + palavra);
    }
  }

  @Test
  @DisplayName("Deve detectar conteúdo sexual/adulto")
  void deveDetectarConteudoSexual() {
    // Arrange
    String[] palavras = {"porno", "pornografia", "xxx", "xvideos", "pornhub"};

    // Act & Assert
    for (String palavra : palavras) {
      assertTrue(service.contemPalavraProibida(palavra),
        "Deveria detectar: " + palavra);
    }
  }

  @Test
  @DisplayName("Deve detectar spam e fraudes")
  void deveDetectarSpam() {
    // Arrange
    String[] palavras = {"viagra", "cialis", "casino", "cassino", "ganhe dinheiro"};

    // Act & Assert
    for (String palavra : palavras) {
      assertTrue(service.contemPalavraProibida(palavra),
        "Deveria detectar: " + palavra);
    }
  }

  @Test
  @DisplayName("Deve detectar termos relacionados a drogas")
  void deveDetectarDrogas() {
    // Arrange
    String[] palavras = {"maconha", "cocaina", "crack", "droga", "traficante"};

    // Act & Assert
    for (String palavra : palavras) {
      assertTrue(service.contemPalavraProibida(palavra),
        "Deveria detectar: " + palavra);
    }
  }

  @Test
  @DisplayName("Deve detectar termos de violência")
  void deveDetectarViolencia() {
    // Arrange
    String[] palavras = {"matar", "assassino", "terrorista", "bomba", "suicidio"};

    // Act & Assert
    for (String palavra : palavras) {
      assertTrue(service.contemPalavraProibida(palavra),
        "Deveria detectar: " + palavra);
    }
  }
}
