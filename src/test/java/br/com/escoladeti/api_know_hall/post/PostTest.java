package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade Post")
class PostTest {

  private Post post;
  private Usuario usuario;
  private Tag tag1;
  private Tag tag2;

  @BeforeEach
  void setUp() {
    // Setup Usuario
    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    // Setup Tags
    tag1 = new Tag();
    tag1.setId(BigInteger.ONE);
    tag1.setName("React Native");

    tag2 = new Tag();
    tag2.setId(BigInteger.TWO);
    tag2.setName("JavaScript");

    // Setup Post
    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Título do Post");
    post.setDescricao("Descrição do post de teste");
    post.setTotalUpVotes(0L);
    post.setUsuario(usuario);
    post.setDataCriacao(Timestamp.from(Instant.now()));
  }

  @Test
  @DisplayName("Deve criar um post com valores válidos")
  void deveCriarPostComValoresValidos() {
    assertThat(post).isNotNull();
    assertThat(post.getId()).isEqualTo(BigInteger.ONE);
    assertThat(post.getTitulo()).isEqualTo("Título do Post");
    assertThat(post.getDescricao()).isEqualTo("Descrição do post de teste");
    assertThat(post.getTotalUpVotes()).isEqualTo(0L);
    assertThat(post.getUsuario()).isEqualTo(usuario);
    assertThat(post.getDataCriacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve inicializar lista de tags vazia")
  void deveInicializarListaDeTagsVazia() {
    Post novoPost = new Post();
    assertThat(novoPost.getTags()).isNotNull();
    assertThat(novoPost.getTags()).isEmpty();
  }

  @Test
  @DisplayName("Deve inicializar lista de comentários vazia")
  void deveInicializarListaDeComentariosVazia() {
    Post novoPost = new Post();
    assertThat(novoPost.getComentarios()).isNotNull();
    assertThat(novoPost.getComentarios()).isEmpty();
  }

  @Test
  @DisplayName("Deve adicionar tags ao post")
  void deveAdicionarTagsAoPost() {
    List<Tag> tags = new ArrayList<>();
    tags.add(tag1);
    tags.add(tag2);

    post.setTags(tags);

    assertThat(post.getTags()).hasSize(2);
    assertThat(post.getTags()).contains(tag1, tag2);
  }

  @Test
  @DisplayName("Deve permitir atualizar título e descrição")
  void devePermitirAtualizarTituloEDescricao() {
    String novoTitulo = "Título Atualizado";
    String novaDescricao = "Descrição Atualizada";

    post.setTitulo(novoTitulo);
    post.setDescricao(novaDescricao);

    assertThat(post.getTitulo()).isEqualTo(novoTitulo);
    assertThat(post.getDescricao()).isEqualTo(novaDescricao);
  }

  @Test
  @DisplayName("Deve permitir incrementar upvotes")
  void devePermitirIncrementarUpvotes() {
    post.setTotalUpVotes(0L);

    post.setTotalUpVotes(post.getTotalUpVotes() + 1);

    assertThat(post.getTotalUpVotes()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Deve manter relacionamento com usuário")
  void deveManterRelacionamentoComUsuario() {
    assertThat(post.getUsuario()).isNotNull();
    assertThat(post.getUsuario().getId()).isEqualTo(BigInteger.ONE);
    assertThat(post.getUsuario().getNome()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("Deve criar post usando construtor com todos os argumentos")
  void deveCriarPostUsandoConstrutorCompleto() {
    List<Tag> tags = List.of(tag1, tag2);
    Timestamp agora = Timestamp.from(Instant.now());

    Post novoPost = new Post(
      BigInteger.TWO,
      tags,
      "Descrição",
      "Título",
      10L,
      usuario,
      agora,
      new ArrayList<>()
    );

    assertThat(novoPost.getId()).isEqualTo(BigInteger.TWO);
    assertThat(novoPost.getTitulo()).isEqualTo("Título");
    assertThat(novoPost.getDescricao()).isEqualTo("Descrição");
    assertThat(novoPost.getTotalUpVotes()).isEqualTo(10L);
    assertThat(novoPost.getUsuario()).isEqualTo(usuario);
    assertThat(novoPost.getTags()).hasSize(2);
    assertThat(novoPost.getComentarios()).isEmpty();  // ✅ Verifica lista de comentários
    assertThat(novoPost.getDataCriacao()).isEqualTo(agora);
  }

  @Test
  @DisplayName("Deve remover todas as tags")
  void deveRemoverTodasAsTags() {
    List<Tag> tags = new ArrayList<>();
    tags.add(tag1);
    tags.add(tag2);
    post.setTags(tags);

    post.getTags().clear();

    assertThat(post.getTags()).isEmpty();
  }
}
