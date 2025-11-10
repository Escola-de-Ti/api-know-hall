package br.com.escoladeti.api_know_hall.historico;

import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoRequestDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.HistoricoTransacao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.repository.HistoricoTransacaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do HistoricoTransacaoService")
class HistoricoTransacaoServiceTest {

  @Mock
  private HistoricoTransacaoRepository historicoTransacaoRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private HistoricoTransacaoService historicoTransacaoService;

  private Usuario usuario;
  private HistoricoTransacao transacao1;
  private HistoricoTransacao transacao2;
  private HistoricoTransacao transacao3;
  private Principal mockPrincipal;
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

    transacao1 = new HistoricoTransacao();
    transacao1.setId(BigInteger.ONE);
    transacao1.setUsuario(usuario);
    transacao1.setQuantidade(200L);
    transacao1.setMotivo(MotivoTransacao.SUPER_VOTE);
    transacao1.setDescricao("Super vote em comentário");
    transacao1.setDataTransacao(agora);

    transacao2 = new HistoricoTransacao();
    transacao2.setId(BigInteger.TWO);
    transacao2.setUsuario(usuario);
    transacao2.setQuantidade(50L);
    transacao2.setMotivo(MotivoTransacao.UP_VOTE_COMENTARIO);
    transacao2.setDescricao("Comentário atingiu 5 upvotes");
    transacao2.setDataTransacao(agora);

    transacao3 = new HistoricoTransacao();
    transacao3.setId(BigInteger.valueOf(3));
    transacao3.setUsuario(usuario);
    transacao3.setQuantidade(-500L);
    transacao3.setMotivo(MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO);
    transacao3.setDescricao("Inscrição em workshop de React");
    transacao3.setDataTransacao(agora);

    mockPrincipal = () -> "joao@email.com";
  }

  @Test
  @DisplayName("Deve registrar transação com sucesso")
  void deveRegistrarTransacaoComSucesso() {
    when(historicoTransacaoRepository.save(any(HistoricoTransacao.class)))
      .thenReturn(transacao1);

    assertThatCode(() ->
      historicoTransacaoService.registrarTransacao(
        usuario,
        200L,
        MotivoTransacao.SUPER_VOTE,
        "Super vote em comentário"
      )
    ).doesNotThrowAnyException();

    verify(historicoTransacaoRepository).save(argThat(h ->
      h.getUsuario().equals(usuario) &&
        h.getQuantidade().equals(200L) &&
        h.getMotivo().equals(MotivoTransacao.SUPER_VOTE) &&
        h.getDescricao().equals("Super vote em comentário")
    ));
  }

  @Test
  @DisplayName("Deve registrar transação negativa (gasto)")
  void deveRegistrarTransacaoNegativa() {
    when(historicoTransacaoRepository.save(any(HistoricoTransacao.class)))
      .thenReturn(transacao3);

    assertThatCode(() ->
      historicoTransacaoService.registrarTransacao(
        usuario,
        -500L,
        MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO,
        "Inscrição em workshop"
      )
    ).doesNotThrowAnyException();

    verify(historicoTransacaoRepository).save(argThat(h ->
      h.getQuantidade().equals(-500L) &&
        h.getMotivo().equals(MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO)
    ));
  }

  @Test
  @DisplayName("Deve registrar transação de conquista")
  void deveRegistrarTransacaoDeConquista() {
    when(historicoTransacaoRepository.save(any(HistoricoTransacao.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    historicoTransacaoService.registrarTransacao(
      usuario,
      100L,
      MotivoTransacao.CONQUISTA,
      "Conquistou badge de contribuidor"
    );

    verify(historicoTransacaoRepository).save(argThat(h ->
      h.getMotivo().equals(MotivoTransacao.CONQUISTA)
    ));
  }

  @Test
  @DisplayName("Deve buscar histórico por principal com sucesso")
  void deveBuscarHistoricoPorPrincipalComSucesso() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 20),
      2
    );

    when(usuarioRepository.findByEmail("joao@email.com"))
      .thenReturn(Optional.of(usuario));
    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(BigInteger.ONE))
      .thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(BigInteger.ONE))
      .thenReturn(-500L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(mockPrincipal, request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.transacoes()).hasSize(2);
    assertThat(resultado.totalRecebido()).isEqualTo(250L);
    assertThat(resultado.totalGasto()).isEqualTo(500L);
    assertThat(resultado.saldoAtual()).isEqualTo(1000L);
    assertThat(resultado.hasMore()).isFalse();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(historicoTransacaoRepository).findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    );
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar histórico de usuário inexistente")
  void deveLancarExcecaoAoBuscarHistoricoDeUsuarioInexistente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
      historicoTransacaoService.buscarHistoricoUsuario(mockPrincipal, request)
    )
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(historicoTransacaoRepository, never()).findByUsuarioIdOrderByDataTransacaoDesc(
      any(),
      any()
    );
  }

  @Test
  @DisplayName("Deve buscar histórico por ID do usuário com sucesso")
  void deveBuscarHistoricoPorIdDoUsuarioComSucesso() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 20),
      2
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(BigInteger.ONE))
      .thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(BigInteger.ONE))
      .thenReturn(-500L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.transacoes()).hasSize(2);

    verify(usuarioRepository).findById(BigInteger.ONE);
    verify(historicoTransacaoRepository).findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    );
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar histórico por ID inexistente")
  void deveLancarExcecaoAoBuscarHistoricoPorIdInexistente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
      historicoTransacaoService.buscarHistoricoUsuario(BigInteger.valueOf(999), request)
    )
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(usuarioRepository).findById(BigInteger.valueOf(999));
  }

  @Test
  @DisplayName("Deve filtrar histórico por motivo")
  void deveFiltrarHistoricoPorMotivo() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, MotivoTransacao.SUPER_VOTE, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdAndMotivo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.SUPER_VOTE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(200L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);
    assertThat(resultado.transacoes().get(0).motivo())
      .isEqualTo(MotivoTransacao.SUPER_VOTE);

    verify(historicoTransacaoRepository).findByUsuarioIdAndMotivo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.SUPER_VOTE),
      any(Pageable.class)
    );
  }

  @Test
  @DisplayName("Deve filtrar histórico por período")
  void deveFiltrarHistoricoPorPeriodo() {
    Timestamp dataInicio = Timestamp.valueOf("2025-01-01 00:00:00");
    Timestamp dataFim = Timestamp.valueOf("2025-01-31 23:59:59");

    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, dataInicio, dataFim
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 20),
      2
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdAndDataTransacaoBetween(
      eq(BigInteger.ONE),
      eq(dataInicio),
      eq(dataFim),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(2);

    verify(historicoTransacaoRepository).findByUsuarioIdAndDataTransacaoBetween(
      eq(BigInteger.ONE),
      eq(dataInicio),
      eq(dataFim),
      any(Pageable.class)
    );
  }

  @Test
  @DisplayName("Deve filtrar histórico por motivo E período")
  void deveFiltrarHistoricoPorMotivoEPeriodo() {
    Timestamp dataInicio = Timestamp.valueOf("2025-01-01 00:00:00");
    Timestamp dataFim = Timestamp.valueOf("2025-01-31 23:59:59");

    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, MotivoTransacao.UP_VOTE_COMENTARIO, dataInicio, dataFim
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao2),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdAndMotivoAndPeriodo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.UP_VOTE_COMENTARIO),
      eq(dataInicio),
      eq(dataFim),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(50L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);

    verify(historicoTransacaoRepository).findByUsuarioIdAndMotivoAndPeriodo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.UP_VOTE_COMENTARIO),
      eq(dataInicio),
      eq(dataFim),
      any(Pageable.class)
    );
  }

  @Test
  @DisplayName("Deve paginar resultados corretamente")
  void devePaginarResultadosCorretamente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 2, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 2),
      5
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(2);
    assertThat(resultado.hasMore()).isTrue();
    assertThat(resultado.totalPages()).isEqualTo(3);
    assertThat(resultado.totalElements()).isEqualTo(5);
  }

  @Test
  @DisplayName("Deve indicar hasMore false na última página")
  void deveIndicarHasMoreFalseNaUltimaPagina() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      1, 2, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao3),
      PageRequest.of(1, 2),
      3
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(-500L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);
    assertThat(resultado.hasMore()).isFalse();
  }

  @Test
  @DisplayName("Deve usar valores padrão de paginação quando não especificados")
  void deveUsarValoresPadraoDePaginacao() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      null, null, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(200L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado).isNotNull();

    verify(historicoTransacaoRepository).findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      argThat(pageable ->
        pageable.getPageNumber() == 0 &&
          pageable.getPageSize() == 20
      )
    );
  }

  @Test
  @DisplayName("Deve calcular total recebido corretamente")
  void deveCalcularTotalRecebidoCorretamente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 20),
      2
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(BigInteger.ONE))
      .thenReturn(1500L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.totalRecebido()).isEqualTo(1500L);

    verify(historicoTransacaoRepository).somarTokensRecebidos(BigInteger.ONE);
  }

  @Test
  @DisplayName("Deve calcular total gasto corretamente")
  void deveCalcularTotalGastoCorretamente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao3),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(0L);
    when(historicoTransacaoRepository.somarTokensGastos(BigInteger.ONE))
      .thenReturn(-800L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.totalGasto()).isEqualTo(800L);

    verify(historicoTransacaoRepository).somarTokensGastos(BigInteger.ONE);
  }

  @Test
  @DisplayName("Deve retornar saldo atual do usuário")
  void deveRetornarSaldoAtualDoUsuario() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1),
      PageRequest.of(0, 20),
      1
    );

    usuario.setQntdToken(2500L);

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(3000L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(-500L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.saldoAtual()).isEqualTo(2500L);
  }

  @Test
  @DisplayName("Deve mapear transação para DTO corretamente")
  void deveMaperarTransacaoParaDTOCorretamente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(200L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);
    HistoricoTransacaoResponseDTO dto = resultado.transacoes().get(0);

    assertThat(dto.id()).isEqualTo(BigInteger.ONE);
    assertThat(dto.quantidade()).isEqualTo(200L);
    assertThat(dto.motivo()).isEqualTo(MotivoTransacao.SUPER_VOTE);
    assertThat(dto.motivoDescricao()).isEqualTo("Super vote em comentário");
    assertThat(dto.descricao()).isEqualTo("Super vote em comentário");
    assertThat(dto.dataTransacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não há transações")
  void deveRetornarListaVaziaQuandoNaoHaTransacoes() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(),
      PageRequest.of(0, 20),
      0
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(0L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).isEmpty();
    assertThat(resultado.hasMore()).isFalse();
    assertThat(resultado.totalRecebido()).isZero();
    assertThat(resultado.totalGasto()).isZero();
  }

  @Test
  @DisplayName("Deve ordenar transações por data decrescente")
  void deveOrdenarTransacoesPorDataDecrescente() {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, null, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacao1, transacao2),
      PageRequest.of(0, 20),
      2
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
      any(), any()
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(250L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    historicoTransacaoService.buscarHistoricoUsuario(BigInteger.ONE, request);

    verify(historicoTransacaoRepository).findByUsuarioIdOrderByDataTransacaoDesc(
      eq(BigInteger.ONE),
      argThat(pageable ->
        pageable.getSort().getOrderFor("dataTransacao") != null &&
          pageable.getSort().getOrderFor("dataTransacao").isDescending()
      )
    );
  }

  @Test
  @DisplayName("Deve buscar transações de resposta destaque")
  void deveBuscarTransacoesDeRespostaDestaque() {
    HistoricoTransacao transacaoDestaque = new HistoricoTransacao();
    transacaoDestaque.setId(BigInteger.valueOf(10));
    transacaoDestaque.setUsuario(usuario);
    transacaoDestaque.setQuantidade(100L);
    transacaoDestaque.setMotivo(MotivoTransacao.RESPOSTA_DESTAQUE);
    transacaoDestaque.setDescricao("Comentário marcado como destaque");
    transacaoDestaque.setDataTransacao(agora);

    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, MotivoTransacao.RESPOSTA_DESTAQUE, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacaoDestaque),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdAndMotivo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.RESPOSTA_DESTAQUE),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(100L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(0L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);
    assertThat(resultado.transacoes().get(0).motivo())
      .isEqualTo(MotivoTransacao.RESPOSTA_DESTAQUE);
  }

  @Test
  @DisplayName("Deve buscar transações de punição")
  void deveBuscarTransacoesDePunicao() {
    HistoricoTransacao transacaoPunicao = new HistoricoTransacao();
    transacaoPunicao.setId(BigInteger.valueOf(11));
    transacaoPunicao.setUsuario(usuario);
    transacaoPunicao.setQuantidade(-100L);
    transacaoPunicao.setMotivo(MotivoTransacao.PUNICAO);
    transacaoPunicao.setDescricao("Punição por denúncia aceita");
    transacaoPunicao.setDataTransacao(agora);

    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      0, 20, MotivoTransacao.PUNICAO, null, null
    );

    Page<HistoricoTransacao> page = new PageImpl<>(
      List.of(transacaoPunicao),
      PageRequest.of(0, 20),
      1
    );

    when(usuarioRepository.findById(BigInteger.ONE))
      .thenReturn(Optional.of(usuario));
    when(historicoTransacaoRepository.findByUsuarioIdAndMotivo(
      eq(BigInteger.ONE),
      eq(MotivoTransacao.PUNICAO),
      any(Pageable.class)
    )).thenReturn(page);
    when(historicoTransacaoRepository.somarTokensRecebidos(any())).thenReturn(0L);
    when(historicoTransacaoRepository.somarTokensGastos(any())).thenReturn(-100L);

    HistoricoTransacaoListResponseDTO resultado = historicoTransacaoService
      .buscarHistoricoUsuario(BigInteger.ONE, request);

    assertThat(resultado.transacoes()).hasSize(1);
    assertThat(resultado.transacoes().get(0).quantidade()).isEqualTo(-100L);
  }
}
