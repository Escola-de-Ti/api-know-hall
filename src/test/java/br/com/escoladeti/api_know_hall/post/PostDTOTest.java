package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoDirecao;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.enums.TagOperador;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes dos DTOs de Post")
class PostDTOTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // ==================== POST CREATE DTO ====================

  @Test
  @DisplayName("PostCreateDTO - Deve criar DTO válido")
  void postCreateDTO_deveCriarDTOValido() {
    PostCreateDTO dto = new PostCreateDTO(
      BigInteger.ONE,
      "Título",
      "Descrição",
      List.of(BigInteger.ONE)
    );

    Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
    assertThat(dto.usuarioId()).isEqualTo(BigInteger.ONE);
    assertThat(dto.titulo()).isEqualTo("Título");
  }

  @Test
  @DisplayName("PostCreateDTO - Deve falhar com usuarioId nulo")
  void postCreateDTO_deveFalharComUsuarioIdNulo() {
    PostCreateDTO dto = new PostCreateDTO(
      null,
      "Título",
      "Descrição",
      null
    );

    Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);

    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> v.getMessage().contains("obrigatório"));
  }

  @Test
  @DisplayName("PostCreateDTO - Deve falhar com título vazio")
  void postCreateDTO_deveFalharComTituloVazio() {
    PostCreateDTO dto = new PostCreateDTO(
      BigInteger.ONE,
      "",
      "Descrição",
      null
    );

    Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);

    assertThat(violations).isNotEmpty();
  }

  // ==================== POST UPDATE DTO ====================

  @Test
  @DisplayName("PostUpdateDTO - Deve criar DTO com todos os campos")
  void postUpdateDTO_deveCriarDTOComTodosCampos() {
    PostUpdateDTO dto = new PostUpdateDTO(
      "Novo Título",
      "Nova Descrição",
      List.of(BigInteger.ONE)
    );

    assertThat(dto.titulo()).isEqualTo("Novo Título");
    assertThat(dto.descricao()).isEqualTo("Nova Descrição");
    assertThat(dto.tagIds()).hasSize(1);
  }

  @Test
  @DisplayName("PostUpdateDTO - Deve permitir campos nulos (atualização parcial)")
  void postUpdateDTO_devePermitirCamposNulos() {
    PostUpdateDTO dto = new PostUpdateDTO(
      "Apenas Título",
      null,
      null
    );

    Set<ConstraintViolation<PostUpdateDTO>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
    assertThat(dto.titulo()).isEqualTo("Apenas Título");
    assertThat(dto.descricao()).isNull();
    assertThat(dto.tagIds()).isNull();
  }

  // ==================== POST RESPONSE DTO ====================

  @Test
  @DisplayName("PostResponseDTO - Deve criar DTO de resposta completo")
  void postResponseDTO_deveCriarDTOCompleto() {
    TagResponseDTO tagDTO = new TagResponseDTO(BigInteger.ONE, "React Native");
    PostResponseDTO dto = new PostResponseDTO(
      BigInteger.ONE,
      BigInteger.TWO,
      "João Silva",
      "Título",
      "Descrição",
      10L,
      List.of(tagDTO),
      Timestamp.from(Instant.now())
    );

    assertThat(dto.id()).isEqualTo(BigInteger.ONE);
    assertThat(dto.usuarioId()).isEqualTo(BigInteger.TWO);
    assertThat(dto.nomeUsuario()).isEqualTo("João Silva");
    assertThat(dto.titulo()).isEqualTo("Título");
    assertThat(dto.totalUpVotes()).isEqualTo(10L);
    assertThat(dto.tags()).hasSize(1);
    assertThat(dto.dataCriacao()).isNotNull();
  }

  // ==================== FEED REQUEST DTO ====================

  @Test
  @DisplayName("FeedRequestDTO - Deve aplicar valores padrão")
  void feedRequestDTO_deveAplicarValoresPadrao() {
    FeedRequestDTO dto = new FeedRequestDTO(
      BigInteger.ONE,
      null,  // Deve usar default 20
      null,
      null,
      null,
      null,  // Deve usar default OR
      null,
      null
    );

    assertThat(dto.pageSize()).isEqualTo(20);
    assertThat(dto.tagOperador()).isEqualTo(TagOperador.OR);
  }

  @Test
  @DisplayName("FeedRequestDTO - Deve limitar pageSize ao máximo")
  void feedRequestDTO_deveLimitarPageSizeAoMaximo() {
    FeedRequestDTO dto = new FeedRequestDTO(
      BigInteger.ONE,
      200,  // Acima do máximo
      null,
      null,
      null,
      TagOperador.OR,
      null,
      null
    );

    assertThat(dto.pageSize()).isEqualTo(100);
  }

  @Test
  @DisplayName("FeedRequestDTO - Deve criar com todos os filtros")
  void feedRequestDTO_deveCriarComTodosFiltros() {
    FeedRequestDTO dto = new FeedRequestDTO(
      BigInteger.ONE,
      10,
      BigInteger.valueOf(5),
      50.0,
      List.of(BigInteger.ONE, BigInteger.TWO),
      TagOperador.AND,
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 10, 22)
    );

    assertThat(dto.usuarioId()).isEqualTo(BigInteger.ONE);
    assertThat(dto.pageSize()).isEqualTo(10);
    assertThat(dto.lastPostId()).isEqualTo(BigInteger.valueOf(5));
    assertThat(dto.lastScore()).isEqualTo(50.0);
    assertThat(dto.tagIds()).hasSize(2);
    assertThat(dto.tagOperador()).isEqualTo(TagOperador.AND);
    assertThat(dto.dataInicio()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(dto.dataFim()).isEqualTo(LocalDate.of(2025, 10, 22));
  }

  // ==================== FEED RESPONSE DTO ====================

  @Test
  @DisplayName("FeedResponseDTO - Deve criar resposta com posts")
  void feedResponseDTO_deveCriarRespostaComPosts() {
    PostFeedDTO feedDTO = new PostFeedDTO(
      BigInteger.ONE, BigInteger.ONE, "João", "Título", "Desc",
      10L, List.of(), Timestamp.from(Instant.now()), 50.0, 2
    );

    FeedResponseDTO dto = new FeedResponseDTO(
      List.of(feedDTO),
      true,
      BigInteger.ONE,
      50.0
    );

    assertThat(dto.posts()).hasSize(1);
    assertThat(dto.hasMore()).isTrue();
    assertThat(dto.lastPostId()).isEqualTo(BigInteger.ONE);
    assertThat(dto.lastScore()).isEqualTo(50.0);
  }

  // ==================== POST BUSCA REQUEST DTO ====================

  @Test
  @DisplayName("PostBuscaRequestDTO - Deve aplicar valores padrão")
  void postBuscaRequestDTO_deveAplicarValoresPadrao() {
    PostBuscaRequestDTO dto = new PostBuscaRequestDTO(
      null,
      null,  // OR
      null,
      null,
      null,  // DATA
      null,  // DESC
      null,  // 20
      null,
      null,
      null   // termo
    );

    assertThat(dto.tagOperador()).isEqualTo(TagOperador.OR);
    assertThat(dto.ordenacao()).isEqualTo(OrdenacaoTipo.DATA);
    assertThat(dto.direcao()).isEqualTo(OrdenacaoDirecao.DESC);
    assertThat(dto.pageSize()).isEqualTo(20);
    assertThat(dto.termo()).isNull();
  }

  @Test
  @DisplayName("PostBuscaRequestDTO - Deve criar com ordenação customizada")
  void postBuscaRequestDTO_deveCriarComOrdenacaoCustomizada() {
    PostBuscaRequestDTO dto = new PostBuscaRequestDTO(
      List.of(BigInteger.ONE),
      TagOperador.AND,
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 12, 31),
      OrdenacaoTipo.VOTOS,
      OrdenacaoDirecao.ASC,
      15,
      BigInteger.valueOf(10),
      100L,
      null  // termo
    );

    assertThat(dto.ordenacao()).isEqualTo(OrdenacaoTipo.VOTOS);
    assertThat(dto.direcao()).isEqualTo(OrdenacaoDirecao.ASC);
    assertThat(dto.pageSize()).isEqualTo(15);
    assertThat(dto.lastValue()).isEqualTo(100L);
    assertThat(dto.termo()).isNull();
  }

  @Test
  @DisplayName("PostBuscaRequestDTO - Deve criar com termo de busca")
  void postBuscaRequestDTO_deveCriarComTermoDeBusca() {
    PostBuscaRequestDTO dto = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.DATA,
      OrdenacaoDirecao.DESC,
      20,
      null,
      null,
      "spring boot"
    );

    assertThat(dto.termo()).isEqualTo("spring boot");
  }

  @Test
  @DisplayName("PostBuscaRequestDTO - Deve aceitar termo vazio")
  void postBuscaRequestDTO_deveAceitarTermoVazio() {
    PostBuscaRequestDTO dto = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.DATA,
      OrdenacaoDirecao.DESC,
      20,
      null,
      null,
      ""
    );

    assertThat(dto.termo()).isEmpty();
  }

  @Test
  @DisplayName("PostBuscaRequestDTO - Deve criar com termo e outros filtros")
  void postBuscaRequestDTO_deveCriarComTermoEOutrosFiltros() {
    PostBuscaRequestDTO dto = new PostBuscaRequestDTO(
      List.of(BigInteger.ONE, BigInteger.TWO),
      TagOperador.AND,
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 12, 31),
      OrdenacaoTipo.VOTOS,
      OrdenacaoDirecao.DESC,
      10,
      null,
      null,
      "react native performance"
    );

    assertThat(dto.termo()).isEqualTo("react native performance");
    assertThat(dto.tagIds()).hasSize(2);
    assertThat(dto.tagOperador()).isEqualTo(TagOperador.AND);
    assertThat(dto.dataInicio()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(dto.dataFim()).isEqualTo(LocalDate.of(2025, 12, 31));
  }

  // ==================== POST FEED DTO ====================

  @Test
  @DisplayName("PostFeedDTO - Deve incluir campos de relevância")
  void postFeedDTO_deveIncluirCamposDeRelevancia() {
    PostFeedDTO dto = new PostFeedDTO(
      BigInteger.ONE,
      BigInteger.TWO,
      "João Silva",
      "Título",
      "Descrição",
      25L,
      List.of(),
      Timestamp.from(Instant.now()),
      75.5,
      3
    );

    assertThat(dto.relevanceScore()).isEqualTo(75.5);
    assertThat(dto.tagsEmComum()).isEqualTo(3);
    assertThat(dto.totalUpVotes()).isEqualTo(25L);
  }

  // ==================== POST BUSCA ITEM DTO ====================

  @Test
  @DisplayName("PostBuscaItemDTO - Deve criar item de busca")
  void postBuscaItemDTO_deveCriarItemDeBusca() {
    PostBuscaItemDTO dto = new PostBuscaItemDTO(
      BigInteger.ONE,
      BigInteger.TWO,
      "Maria Santos",
      "Post de Teste",
      "Descrição detalhada",
      50L,
      List.of(new TagResponseDTO(BigInteger.ONE, "Java")),
      Timestamp.from(Instant.now())
    );

    assertThat(dto.id()).isEqualTo(BigInteger.ONE);
    assertThat(dto.usuarioId()).isEqualTo(BigInteger.TWO);
    assertThat(dto.nomeUsuario()).isEqualTo("Maria Santos");
    assertThat(dto.totalUpVotes()).isEqualTo(50L);
    assertThat(dto.tags()).hasSize(1);
  }

  // ==================== POST BUSCA RESPONSE DTO ====================

  @Test
  @DisplayName("PostBuscaResponseDTO - Deve criar resposta de busca")
  void postBuscaResponseDTO_deveCriarRespostaDeBusca() {
    PostBuscaItemDTO item = new PostBuscaItemDTO(
      BigInteger.ONE,
      BigInteger.ONE,
      "João",
      "Título",
      "Desc",
      10L,
      List.of(),
      Timestamp.from(Instant.now())
    );

    PostBuscaResponseDTO dto = new PostBuscaResponseDTO(
      List.of(item),
      true,
      BigInteger.ONE,
      10L
    );

    assertThat(dto.posts()).hasSize(1);
    assertThat(dto.hasMore()).isTrue();
    assertThat(dto.lastPostId()).isEqualTo(BigInteger.ONE);
    assertThat(dto.lastValue()).isEqualTo(10L);
  }

  @Test
  @DisplayName("PostBuscaResponseDTO - Deve criar resposta vazia")
  void postBuscaResponseDTO_deveCriarRespostaVazia() {
    PostBuscaResponseDTO dto = new PostBuscaResponseDTO(
      List.of(),
      false,
      null,
      null
    );

    assertThat(dto.posts()).isEmpty();
    assertThat(dto.hasMore()).isFalse();
    assertThat(dto.lastPostId()).isNull();
    assertThat(dto.lastValue()).isNull();
  }
}
