package br.com.escoladeti.api_know_hall.comentario;

import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade Comentario")
class ComentarioTest {

  private Comentario comentario;
  private Post post;
  private Usuario usuario;
  private Comentario comentarioPai;

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

    // Setup Post
    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Post de Teste");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(10L);
    post.setUsuario(usuario);
    post.setComentarios(new ArrayList<>());
    post.setDataCriacao(Timestamp.from(Instant.now()));

    // Setup Comentario Pai
    comentarioPai = new Comentario();
    comentarioPai.setId(BigInteger.ONE);
    comentarioPai.setTexto("Comentário pai");
    comentarioPai.setPost(post);
    comentarioPai.setUsuario(usuario);
    comentarioPai.setTotalUpVotes(5L);
    comentarioPai.setTotalSuperVotes(2L);
    comentarioPai.setDataCriacao(Timestamp.from(Instant.now()));

    // Setup Comentario
    comentario = new Comentario();
    comentario.setId(BigInteger.TWO);
    comentario.setTexto("Comentário de teste");
    comentario.setPost(post);
    comentario.setUsuario(usuario);
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);
    comentario.setDataCriacao(Timestamp.from(Instant.now()));
  }

  @Test
  @DisplayName("Deve criar um comentário com valores válidos")
  void deveCriarComentarioComValoresValidos() {
    assertThat(comentario).isNotNull();
    assertThat(comentario.getId()).isEqualTo(BigInteger.TWO);
    assertThat(comentario.getTexto()).isEqualTo("Comentário de teste");
    assertThat(comentario.getPost()).isEqualTo(post);
    assertThat(comentario.getUsuario()).isEqualTo(usuario);
    assertThat(comentario.getTotalUpVotes()).isEqualTo(0L);
    assertThat(comentario.getTotalSuperVotes()).isEqualTo(0L);
    assertThat(comentario.getDataCriacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve inicializar lista de respostas vazia")
  void deveInicializarListaDeRespostasVazia() {
    Comentario novoComentario = new Comentario();
    assertThat(novoComentario.getRespostas()).isNotNull();
    assertThat(novoComentario.getRespostas()).isEmpty();
  }

  @Test
  @DisplayName("Deve permitir definir comentário pai")
  void devePermitirDefinirComentarioPai() {
    comentario.setComentarioPai(comentarioPai);

    assertThat(comentario.getComentarioPai()).isNotNull();
    assertThat(comentario.getComentarioPai().getId()).isEqualTo(BigInteger.ONE);
    assertThat(comentario.getComentarioPai().getTexto()).isEqualTo("Comentário pai");
  }

  @Test
  @DisplayName("Deve permitir comentário sem pai")
  void devePermitirComentarioSemPai() {
    assertThat(comentario.getComentarioPai()).isNull();
  }

  @Test
  @DisplayName("Deve permitir adicionar respostas ao comentário")
  void devePermitirAdicionarRespostasAoComentario() {
    Comentario resposta1 = new Comentario();
    resposta1.setId(BigInteger.valueOf(3));
    resposta1.setTexto("Resposta 1");
    resposta1.setComentarioPai(comentario);

    Comentario resposta2 = new Comentario();
    resposta2.setId(BigInteger.valueOf(4));
    resposta2.setTexto("Resposta 2");
    resposta2.setComentarioPai(comentario);

    comentario.getRespostas().add(resposta1);
    comentario.getRespostas().add(resposta2);

    assertThat(comentario.getRespostas()).hasSize(2);
    assertThat(comentario.getRespostas()).contains(resposta1, resposta2);
  }

  @Test
  @DisplayName("Deve permitir atualizar o texto do comentário")
  void devePermitirAtualizarTextoDoComentario() {
    String novoTexto = "Texto atualizado do comentário";
    comentario.setTexto(novoTexto);

    assertThat(comentario.getTexto()).isEqualTo(novoTexto);
  }

  @Test
  @DisplayName("Deve permitir incrementar upvotes")
  void devePermitirIncrementarUpvotes() {
    comentario.setTotalUpVotes(0L);
    comentario.setTotalUpVotes(comentario.getTotalUpVotes() + 1);

    assertThat(comentario.getTotalUpVotes()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Deve permitir incrementar supervotes")
  void devePermitirIncrementarSupervotes() {
    comentario.setTotalSuperVotes(0L);
    comentario.setTotalSuperVotes(comentario.getTotalSuperVotes() + 1);

    assertThat(comentario.getTotalSuperVotes()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Deve manter relacionamento com post")
  void deveManterRelacionamentoComPost() {
    assertThat(comentario.getPost()).isNotNull();
    assertThat(comentario.getPost().getId()).isEqualTo(BigInteger.ONE);
    assertThat(comentario.getPost().getTitulo()).isEqualTo("Post de Teste");
  }

  @Test
  @DisplayName("Deve manter relacionamento com usuário")
  void deveManterRelacionamentoComUsuario() {
    assertThat(comentario.getUsuario()).isNotNull();
    assertThat(comentario.getUsuario().getId()).isEqualTo(BigInteger.ONE);
    assertThat(comentario.getUsuario().getNome()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("Deve criar comentário usando setters")
  void deveCriarComentarioUsandoSetters() {
    Comentario novoComentario = new Comentario();
    novoComentario.setId(BigInteger.valueOf(5));
    novoComentario.setTexto("Novo comentário");
    novoComentario.setPost(post);
    novoComentario.setUsuario(usuario);
    novoComentario.setTotalUpVotes(10L);
    novoComentario.setTotalSuperVotes(3L);
    novoComentario.setComentarioPai(comentarioPai);
    novoComentario.setDataCriacao(Timestamp.from(Instant.now()));

    assertThat(novoComentario.getId()).isEqualTo(BigInteger.valueOf(5));
    assertThat(novoComentario.getTexto()).isEqualTo("Novo comentário");
    assertThat(novoComentario.getPost()).isEqualTo(post);
    assertThat(novoComentario.getUsuario()).isEqualTo(usuario);
    assertThat(novoComentario.getTotalUpVotes()).isEqualTo(10L);
    assertThat(novoComentario.getTotalSuperVotes()).isEqualTo(3L);
    assertThat(novoComentario.getComentarioPai()).isEqualTo(comentarioPai);
    assertThat(novoComentario.getDataCriacao()).isNotNull();
  }
}
