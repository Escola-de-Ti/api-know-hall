package br.com.escoladeti.api_know_hall.voto;

import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da Entidade Voto")
class VotoTest {

  private Voto voto;
  private Usuario usuario;
  private Post post;
  private Comentario comentario;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Post de Teste");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(10L);
    post.setUsuario(usuario);
    post.setComentarios(new ArrayList<>());
    post.setDataCriacao(Timestamp.from(Instant.now()));

    comentario = new Comentario();
    comentario.setId(BigInteger.ONE);
    comentario.setTexto("Comentário de teste");
    comentario.setPost(post);
    comentario.setUsuario(usuario);
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);
    comentario.setDataCriacao(Timestamp.from(Instant.now()));

    voto = new Voto();
    voto.setId(BigInteger.ONE);
    voto.setUsuario(usuario);
    voto.setPost(post);
    voto.setTipo(TipoVoto.UP_VOTE);
    voto.setDataCriacao(Timestamp.from(Instant.now()));
  }

  @Test
  @DisplayName("Deve criar um voto com valores válidos")
  void deveCriarVotoComValoresValidos() {
    assertThat(voto).isNotNull();
    assertThat(voto.getId()).isEqualTo(BigInteger.ONE);
    assertThat(voto.getUsuario()).isEqualTo(usuario);
    assertThat(voto.getPost()).isEqualTo(post);
    assertThat(voto.getTipo()).isEqualTo(TipoVoto.UP_VOTE);
    assertThat(voto.getDataCriacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve criar voto associado a um Post")
  void deveCriarVotoAssociadoAPost() {
    Voto votoPost = new Voto();
    votoPost.setId(BigInteger.valueOf(2));
    votoPost.setUsuario(usuario);
    votoPost.setPost(post);
    votoPost.setTipo(TipoVoto.UP_VOTE);
    votoPost.setDataCriacao(Timestamp.from(Instant.now()));

    assertThat(votoPost.getPost()).isNotNull();
    assertThat(votoPost.getPost().getId()).isEqualTo(BigInteger.ONE);
    assertThat(votoPost.getComentario()).isNull();
  }

  @Test
  @DisplayName("Deve criar voto associado a um Comentário")
  void deveCriarVotoAssociadoAComentario() {
    Voto votoComentario = new Voto();
    votoComentario.setId(BigInteger.valueOf(3));
    votoComentario.setUsuario(usuario);
    votoComentario.setComentario(comentario);
    votoComentario.setTipo(TipoVoto.SUPER_VOTE);
    votoComentario.setDataCriacao(Timestamp.from(Instant.now()));

    assertThat(votoComentario.getComentario()).isNotNull();
    assertThat(votoComentario.getComentario().getId()).isEqualTo(BigInteger.ONE);
    assertThat(votoComentario.getPost()).isNull();
  }

  @Test
  @DisplayName("Deve manter relacionamento com usuário")
  void deveManterRelacionamentoComUsuario() {
    assertThat(voto.getUsuario()).isNotNull();
    assertThat(voto.getUsuario().getId()).isEqualTo(BigInteger.ONE);
    assertThat(voto.getUsuario().getNome()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("Deve permitir diferentes tipos de voto")
  void devePermitirDiferentesTiposDeVoto() {
    voto.setTipo(TipoVoto.UP_VOTE);
    assertThat(voto.getTipo()).isEqualTo(TipoVoto.UP_VOTE);

    voto.setTipo(TipoVoto.SUPER_VOTE);
    assertThat(voto.getTipo()).isEqualTo(TipoVoto.SUPER_VOTE);
  }

  @Test
  @DisplayName("Deve lançar exceção ao validar voto sem Post e sem Comentário")
  void deveLancarExcecaoAoValidarVotoSemPostESemComentario() {
    Voto votoInvalido = new Voto();
    votoInvalido.setId(BigInteger.valueOf(4));
    votoInvalido.setUsuario(usuario);
    votoInvalido.setTipo(TipoVoto.UP_VOTE);
    votoInvalido.setDataCriacao(Timestamp.from(Instant.now()));

    assertThatThrownBy(votoInvalido::validateVoto)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Voto deve ser associado a um Post OU a um Comentário");
  }

  @Test
  @DisplayName("Deve lançar exceção ao validar voto com Post E Comentário")
  void deveLancarExcecaoAoValidarVotoComPostEComentario() {
    Voto votoInvalido = new Voto();
    votoInvalido.setId(BigInteger.valueOf(5));
    votoInvalido.setUsuario(usuario);
    votoInvalido.setPost(post);
    votoInvalido.setComentario(comentario);
    votoInvalido.setTipo(TipoVoto.UP_VOTE);
    votoInvalido.setDataCriacao(Timestamp.from(Instant.now()));

    assertThatThrownBy(votoInvalido::validateVoto)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Voto deve ser associado a um Post OU a um Comentário");
  }

  @Test
  @DisplayName("Deve criar voto usando setters")
  void deveCriarVotoUsandoSetters() {
    Voto novoVoto = new Voto();
    novoVoto.setId(BigInteger.valueOf(6));
    novoVoto.setUsuario(usuario);
    novoVoto.setPost(post);
    novoVoto.setTipo(TipoVoto.SUPER_VOTE);
    novoVoto.setDataCriacao(Timestamp.from(Instant.now()));

    assertThat(novoVoto.getId()).isEqualTo(BigInteger.valueOf(6));
    assertThat(novoVoto.getUsuario()).isEqualTo(usuario);
    assertThat(novoVoto.getPost()).isEqualTo(post);
    assertThat(novoVoto.getTipo()).isEqualTo(TipoVoto.SUPER_VOTE);
    assertThat(novoVoto.getDataCriacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve permitir alterar tipo de voto")
  void devePermitirAlterarTipoDeVoto() {
    voto.setTipo(TipoVoto.UP_VOTE);
    assertThat(voto.getTipo()).isEqualTo(TipoVoto.UP_VOTE);

    voto.setTipo(TipoVoto.SUPER_VOTE);
    assertThat(voto.getTipo()).isEqualTo(TipoVoto.SUPER_VOTE);
  }

  @Test
  @DisplayName("Deve criar voto para comentário com SUPERVOTE")
  void deveCriarVotoParaComentarioComSupervote() {
    Voto votoSuper = new Voto();
    votoSuper.setId(BigInteger.valueOf(7));
    votoSuper.setUsuario(usuario);
    votoSuper.setComentario(comentario);
    votoSuper.setTipo(TipoVoto.SUPER_VOTE);
    votoSuper.setDataCriacao(Timestamp.from(Instant.now()));

    assertThat(votoSuper.getComentario()).isNotNull();
    assertThat(votoSuper.getTipo()).isEqualTo(TipoVoto.SUPER_VOTE);
    assertThat(votoSuper.getPost()).isNull();
  }

  @Test
  @DisplayName("Deve manter data de criação não nula")
  void deveManterDataDeCriacaoNaoNula() {
    assertThat(voto.getDataCriacao()).isNotNull();
    assertThat(voto.getDataCriacao()).isBeforeOrEqualTo(Timestamp.from(Instant.now()));
  }

  @Test
  @DisplayName("Deve criar voto usando construtor com parâmetros")
  void deveCriarVotoUsandoConstrutorComParametros() {
    Timestamp agora = Timestamp.from(Instant.now());
    Voto novoVoto = new Voto(
      BigInteger.valueOf(8),
      usuario,
      post,
      null,
      TipoVoto.UP_VOTE,
      agora
    );

    assertThat(novoVoto.getId()).isEqualTo(BigInteger.valueOf(8));
    assertThat(novoVoto.getUsuario()).isEqualTo(usuario);
    assertThat(novoVoto.getPost()).isEqualTo(post);
    assertThat(novoVoto.getComentario()).isNull();
    assertThat(novoVoto.getTipo()).isEqualTo(TipoVoto.UP_VOTE);
    assertThat(novoVoto.getDataCriacao()).isEqualTo(agora);
  }

  @Test
  @DisplayName("Deve criar voto usando construtor padrão")
  void deveCriarVotoUsandoConstrutorPadrao() {
    Voto novoVoto = new Voto();

    assertThat(novoVoto).isNotNull();
    assertThat(novoVoto.getId()).isNull();
    assertThat(novoVoto.getUsuario()).isNull();
    assertThat(novoVoto.getPost()).isNull();
    assertThat(novoVoto.getComentario()).isNull();
    assertThat(novoVoto.getTipo()).isNull();
  }
}
