package br.com.escoladeti.api_know_hall.historico;

import br.com.escoladeti.api_know_hall.entity.HistoricoTransacao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade HistoricoTransacao")
class HistoricoTransacaoTest {

  private HistoricoTransacao transacao;
  private Usuario usuario;
  private Timestamp agora;

  @BeforeEach
  void setUp() {
    agora = Timestamp.from(Instant.now());

    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setQntdToken(1000L);
    usuario.setQntdXp(1000L);

    transacao = new HistoricoTransacao();
    transacao.setId(BigInteger.ONE);
    transacao.setUsuario(usuario);
    transacao.setQuantidade(200L);
    transacao.setMotivo(MotivoTransacao.SUPER_VOTE);
    transacao.setDescricao("Super vote em comentário");
    transacao.setDataTransacao(agora);
  }

  @Test
  @DisplayName("Deve criar uma transação com valores válidos")
  void deveCriarTransacaoComValoresValidos() {
    assertThat(transacao).isNotNull();
    assertThat(transacao.getId()).isEqualTo(BigInteger.ONE);
    assertThat(transacao.getUsuario()).isEqualTo(usuario);
    assertThat(transacao.getQuantidade()).isEqualTo(200L);
    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.SUPER_VOTE);
    assertThat(transacao.getDescricao()).isEqualTo("Super vote em comentário");
    assertThat(transacao.getDataTransacao()).isEqualTo(agora);
  }

  @Test
  @DisplayName("Deve criar transação usando construtor com todos os argumentos")
  void deveCriarTransacaoUsandoConstrutorCompleto() {
    HistoricoTransacao novaTransacao = new HistoricoTransacao(
      BigInteger.TWO,
      usuario,
      150L,
      MotivoTransacao.UP_VOTE_COMENTARIO,
      "Comentário atingiu 5 upvotes",
      agora
    );

    assertThat(novaTransacao.getId()).isEqualTo(BigInteger.TWO);
    assertThat(novaTransacao.getUsuario()).isEqualTo(usuario);
    assertThat(novaTransacao.getQuantidade()).isEqualTo(150L);
    assertThat(novaTransacao.getMotivo()).isEqualTo(MotivoTransacao.UP_VOTE_COMENTARIO);
    assertThat(novaTransacao.getDescricao()).isEqualTo("Comentário atingiu 5 upvotes");
    assertThat(novaTransacao.getDataTransacao()).isEqualTo(agora);
  }

  @Test
  @DisplayName("Deve manter relacionamento com usuário")
  void deveManterRelacionamentoComUsuario() {
    assertThat(transacao.getUsuario()).isNotNull();
    assertThat(transacao.getUsuario().getId()).isEqualTo(BigInteger.ONE);
    assertThat(transacao.getUsuario().getNome()).isEqualTo("João Silva");
    assertThat(transacao.getUsuario().getEmail()).isEqualTo("joao@email.com");
  }

  @Test
  @DisplayName("Deve permitir quantidade positiva (ganho de tokens)")
  void devePermitirQuantidadePositiva() {
    transacao.setQuantidade(100L);

    assertThat(transacao.getQuantidade()).isEqualTo(100L);
    assertThat(transacao.getQuantidade()).isPositive();
  }

  @Test
  @DisplayName("Deve permitir quantidade negativa (gasto de tokens)")
  void devePermitirQuantidadeNegativa() {
    transacao.setQuantidade(-500L);

    assertThat(transacao.getQuantidade()).isEqualTo(-500L);
    assertThat(transacao.getQuantidade()).isNegative();
  }

  @Test
  @DisplayName("Deve permitir quantidade zero")
  void devePermitirQuantidadeZero() {
    transacao.setQuantidade(0L);

    assertThat(transacao.getQuantidade()).isZero();
  }

  @Test
  @DisplayName("Deve criar transação com motivo UP_VOTE_COMENTARIO")
  void deveCriarTransacaoComMotivoUpVoteComentario() {
    transacao.setMotivo(MotivoTransacao.UP_VOTE_COMENTARIO);
    transacao.setQuantidade(50L);
    transacao.setDescricao("Comentário atingiu 5 upvotes - 1 marco conquistado");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.UP_VOTE_COMENTARIO);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Up vote em comentário");
  }

  @Test
  @DisplayName("Deve criar transação com motivo SUPER_VOTE")
  void deveCriarTransacaoComMotivoSuperVote() {
    transacao.setMotivo(MotivoTransacao.SUPER_VOTE);
    transacao.setQuantidade(200L);
    transacao.setDescricao("Comentário recebeu Super Vote do autor do post");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.SUPER_VOTE);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Super vote em comentário");
  }

  @Test
  @DisplayName("Deve criar transação com motivo UP_VOTE_POST")
  void deveCriarTransacaoComMotivoUpVotePost() {
    transacao.setMotivo(MotivoTransacao.UP_VOTE_POST);
    transacao.setQuantidade(100L);
    transacao.setDescricao("Post atingiu 25 upvotes - 1 marco conquistado");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.UP_VOTE_POST);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Up vote em post");
  }

  @Test
  @DisplayName("Deve criar transação com motivo CONQUISTA")
  void deveCriarTransacaoComMotivoConquista() {
    transacao.setMotivo(MotivoTransacao.CONQUISTA);
    transacao.setQuantidade(150L);
    transacao.setDescricao("Conquistou badge de Contribuidor Ativo");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.CONQUISTA);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Conquista desbloqueada");
  }

  @Test
  @DisplayName("Deve criar transação com motivo RESPOSTA_DESTAQUE")
  void deveCriarTransacaoComMotivoRespostaDestaque() {
    transacao.setMotivo(MotivoTransacao.RESPOSTA_DESTAQUE);
    transacao.setQuantidade(100L);
    transacao.setDescricao("Comentário marcado como Resposta Destaque");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.RESPOSTA_DESTAQUE);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Resposta marcada como destaque");
  }

  @Test
  @DisplayName("Deve criar transação com motivo GERADOR_QUALIDADE")
  void deveCriarTransacaoComMotivoGeradorQualidade() {
    transacao.setMotivo(MotivoTransacao.GERADOR_QUALIDADE);
    transacao.setQuantidade(100L);
    transacao.setDescricao("Post gerou primeira Resposta Destaque");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.GERADOR_QUALIDADE);
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Post gerou resposta destaque");
  }

  @Test
  @DisplayName("Deve criar transação com motivo INSCRICAO_WORKSHOP_ALUNO (negativa)")
  void deveCriarTransacaoComMotivoInscricaoWorkshopAluno() {
    transacao.setMotivo(MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO);
    transacao.setQuantidade(-500L);
    transacao.setDescricao("Inscrição em workshop de React Native");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO);
    assertThat(transacao.getQuantidade()).isNegative();
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Inscrição em workshop como aluno");
  }

  @Test
  @DisplayName("Deve criar transação com motivo INSCRICAO_WORKSHOP_INSTRUTOR")
  void deveCriarTransacaoComMotivoInscricaoWorkshopInstrutor() {
    transacao.setMotivo(MotivoTransacao.INSCRICAO_WORKSHOP_INSTRUTOR);
    transacao.setQuantidade(500L);
    transacao.setDescricao("Pagamento recebido por workshop ministrado");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.INSCRICAO_WORKSHOP_INSTRUTOR);
    assertThat(transacao.getQuantidade()).isPositive();
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Pagamento recebido por workshop");
  }

  @Test
  @DisplayName("Deve criar transação com motivo PUNICAO (negativa)")
  void deveCriarTransacaoComMotivoPunicao() {
    transacao.setMotivo(MotivoTransacao.PUNICAO);
    transacao.setQuantidade(-100L);
    transacao.setDescricao("Punição por denúncia aceita");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.PUNICAO);
    assertThat(transacao.getQuantidade()).isNegative();
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Punição por denúncia aceita");
  }

  @Test
  @DisplayName("Deve criar transação com motivo STREAK_LOGIN")
  void deveCriarTransacaoComMotivoStreakLogin() {
    transacao.setMotivo(MotivoTransacao.STREAK_LOGIN);
    transacao.setQuantidade(50L);
    transacao.setDescricao("Bônus de 7 dias consecutivos de login");

    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.STREAK_LOGIN);
    assertThat(transacao.getQuantidade()).isPositive();
    assertThat(transacao.getMotivo().getDescricao()).isEqualTo("Bônus por login consecutivo");
  }

  @Test
  @DisplayName("Deve permitir atualizar descrição")
  void devePermitirAtualizarDescricao() {
    String novaDescricao = "Descrição atualizada da transação";

    transacao.setDescricao(novaDescricao);

    assertThat(transacao.getDescricao()).isEqualTo(novaDescricao);
  }

  @Test
  @DisplayName("Deve permitir descrição nula")
  void devePermitirDescricaoNula() {
    transacao.setDescricao(null);

    assertThat(transacao.getDescricao()).isNull();
  }

  @Test
  @DisplayName("Deve permitir descrição vazia")
  void devePermitirDescricaoVazia() {
    transacao.setDescricao("");

    assertThat(transacao.getDescricao()).isEmpty();
  }

  @Test
  @DisplayName("Deve criar transação com descrição longa")
  void deveCriarTransacaoComDescricaoLonga() {
    String descricaoLonga = "A".repeat(500);

    transacao.setDescricao(descricaoLonga);

    assertThat(transacao.getDescricao()).hasSize(500);
  }

  @Test
  @DisplayName("Deve manter data de criação imutável")
  void deveManterDataDeCriacaoImutavel() {
    Timestamp dataOriginal = transacao.getDataTransacao();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    transacao.setQuantidade(300L);
    transacao.setDescricao("Nova descrição");

    assertThat(transacao.getDataTransacao()).isEqualTo(dataOriginal);
  }

  @Test
  @DisplayName("Deve criar múltiplas transações para o mesmo usuário")
  void deveCriarMultiplasTransacoesParaMesmoUsuario() {
    HistoricoTransacao transacao1 = new HistoricoTransacao();
    transacao1.setId(BigInteger.ONE);
    transacao1.setUsuario(usuario);
    transacao1.setQuantidade(100L);
    transacao1.setMotivo(MotivoTransacao.UP_VOTE_COMENTARIO);
    transacao1.setDataTransacao(agora);

    HistoricoTransacao transacao2 = new HistoricoTransacao();
    transacao2.setId(BigInteger.TWO);
    transacao2.setUsuario(usuario);
    transacao2.setQuantidade(200L);
    transacao2.setMotivo(MotivoTransacao.SUPER_VOTE);
    transacao2.setDataTransacao(agora);

    assertThat(transacao1.getUsuario()).isEqualTo(transacao2.getUsuario());
    assertThat(transacao1.getId()).isNotEqualTo(transacao2.getId());
    assertThat(transacao1.getQuantidade()).isNotEqualTo(transacao2.getQuantidade());
  }

  @Test
  @DisplayName("Deve criar transação usando construtor sem argumentos")
  void deveCriarTransacaoUsandoConstrutorSemArgumentos() {
    HistoricoTransacao novaTransacao = new HistoricoTransacao();

    assertThat(novaTransacao).isNotNull();
    assertThat(novaTransacao.getId()).isNull();
    assertThat(novaTransacao.getUsuario()).isNull();
    assertThat(novaTransacao.getQuantidade()).isNull();
    assertThat(novaTransacao.getMotivo()).isNull();
    assertThat(novaTransacao.getDescricao()).isNull();
    assertThat(novaTransacao.getDataTransacao()).isNull();
  }

  @Test
  @DisplayName("Deve validar todos os motivos do enum")
  void deveValidarTodosMotivosDEnum() {
    assertThat(MotivoTransacao.values()).hasSize(13);
    assertThat(MotivoTransacao.values()).contains(
      MotivoTransacao.UP_VOTE_COMENTARIO,
      MotivoTransacao.SUPER_VOTE,
      MotivoTransacao.UP_VOTE_POST,
      MotivoTransacao.CONQUISTA,
      MotivoTransacao.RESPOSTA_DESTAQUE,
      MotivoTransacao.GERADOR_QUALIDADE,
      MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO,
      MotivoTransacao.INSCRICAO_WORKSHOP_INSTRUTOR,
      MotivoTransacao.CANCELAMENTO_WORKSHOP_ALUNO,
      MotivoTransacao.CANCELAMENTO_WORKSHOP_INSTRUTOR,
      MotivoTransacao.PUNICAO,
      MotivoTransacao.STREAK_LOGIN,
      MotivoTransacao.LEVEL_UP
    );
  }

  @Test
  @DisplayName("Deve retornar descrição correta para cada motivo")
  void deveRetornarDescricaoCorretaParaCadaMotivo() {
    assertThat(MotivoTransacao.UP_VOTE_COMENTARIO.getDescricao()).isEqualTo("Up vote em comentário");
    assertThat(MotivoTransacao.SUPER_VOTE.getDescricao()).isEqualTo("Super vote em comentário");
    assertThat(MotivoTransacao.UP_VOTE_POST.getDescricao()).isEqualTo("Up vote em post");
    assertThat(MotivoTransacao.CONQUISTA.getDescricao()).isEqualTo("Conquista desbloqueada");
    assertThat(MotivoTransacao.RESPOSTA_DESTAQUE.getDescricao()).isEqualTo("Resposta marcada como destaque");
    assertThat(MotivoTransacao.GERADOR_QUALIDADE.getDescricao()).isEqualTo("Post gerou resposta destaque");
    assertThat(MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO.getDescricao()).isEqualTo("Inscrição em workshop como aluno");
    assertThat(MotivoTransacao.INSCRICAO_WORKSHOP_INSTRUTOR.getDescricao()).isEqualTo("Pagamento recebido por workshop");
    assertThat(MotivoTransacao.CANCELAMENTO_WORKSHOP_ALUNO.getDescricao()).isEqualTo("Cancelamento de inscrição em workshop como aluno");
    assertThat(MotivoTransacao.CANCELAMENTO_WORKSHOP_INSTRUTOR.getDescricao()).isEqualTo("Reembolso pago por workshop");
    assertThat(MotivoTransacao.PUNICAO.getDescricao()).isEqualTo("Punição por denúncia aceita");
    assertThat(MotivoTransacao.STREAK_LOGIN.getDescricao()).isEqualTo("Bônus por login consecutivo");
  }

  @Test
  @DisplayName("Deve permitir alterar quantidade de positiva para negativa")
  void devePermitirAlterarQuantidadeDePositivaParaNegativa() {
    transacao.setQuantidade(100L);
    assertThat(transacao.getQuantidade()).isPositive();

    transacao.setQuantidade(-100L);
    assertThat(transacao.getQuantidade()).isNegative();
  }

  @Test
  @DisplayName("Deve permitir alterar motivo da transação")
  void devePermitirAlterarMotivoDaTransacao() {
    transacao.setMotivo(MotivoTransacao.SUPER_VOTE);
    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.SUPER_VOTE);

    transacao.setMotivo(MotivoTransacao.CONQUISTA);
    assertThat(transacao.getMotivo()).isEqualTo(MotivoTransacao.CONQUISTA);
  }

  @Test
  @DisplayName("Deve manter integridade entre usuário e transação")
  void deveManterIntegridadeEntreUsuarioETransacao() {
    Usuario novoUsuario = new Usuario();
    novoUsuario.setId(BigInteger.TWO);
    novoUsuario.setNome("Maria Santos");
    novoUsuario.setEmail("maria@email.com");

    transacao.setUsuario(novoUsuario);

    assertThat(transacao.getUsuario()).isEqualTo(novoUsuario);
    assertThat(transacao.getUsuario().getId()).isEqualTo(BigInteger.TWO);
    assertThat(transacao.getUsuario().getNome()).isEqualTo("Maria Santos");
  }
}
