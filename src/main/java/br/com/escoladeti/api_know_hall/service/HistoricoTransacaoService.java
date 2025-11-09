package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoRequestDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.HistoricoTransacao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.repository.HistoricoTransacaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricoTransacaoService {

  private final HistoricoTransacaoRepository historicoTransacaoRepository;
  private final UsuarioRepository usuarioRepository;

  /**
   * Registra uma transação de tokens
   * Usa REQUIRES_NEW para garantir que o registro seja salvo mesmo se a transação pai falhar
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarTransacao(
    Usuario usuario,
    Long quantidade,
    MotivoTransacao motivo,
    String descricao
  ) {
    HistoricoTransacao historico = new HistoricoTransacao();
    historico.setUsuario(usuario);
    historico.setQuantidade(quantidade);
    historico.setMotivo(motivo);
    historico.setDescricao(descricao);

    historicoTransacaoRepository.save(historico);
  }

  /**
   * Busca o histórico de transações do usuário logado
   * Suporta filtros por motivo e período, com paginação
   */
  @Transactional(readOnly = true)
  public HistoricoTransacaoListResponseDTO buscarHistoricoUsuario(
    Principal principal,
    HistoricoTransacaoRequestDTO request
  ) {
    Usuario usuario = usuarioRepository.findByEmail(principal.getName())
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    return buscarHistoricoUsuario(usuario.getId(), request);
  }

  /**
   * Busca o histórico de transações por ID do usuário
   * Método auxiliar que pode ser usado internamente
   */
  @Transactional(readOnly = true)
  public HistoricoTransacaoListResponseDTO buscarHistoricoUsuario(
    BigInteger usuarioId,
    HistoricoTransacaoRequestDTO request
  ) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    Pageable pageable = PageRequest.of(
      request.page(),
      request.size(),
      Sort.by(Sort.Direction.DESC, "dataTransacao")
    );

    Page<HistoricoTransacao> page;

    // Aplica filtros conforme necessário
    if (request.motivo() != null && request.dataInicio() != null && request.dataFim() != null) {
      // Filtro por motivo E período
      page = historicoTransacaoRepository.findByUsuarioIdAndMotivoAndPeriodo(
        usuarioId,
        request.motivo(),
        request.dataInicio(),
        request.dataFim(),
        pageable
      );
    } else if (request.motivo() != null) {
      // Filtro apenas por motivo
      page = historicoTransacaoRepository.findByUsuarioIdAndMotivo(
        usuarioId,
        request.motivo(),
        pageable
      );
    } else if (request.dataInicio() != null && request.dataFim() != null) {
      // Filtro apenas por período
      page = historicoTransacaoRepository.findByUsuarioIdAndDataTransacaoBetween(
        usuarioId,
        request.dataInicio(),
        request.dataFim(),
        pageable
      );
    } else {
      // Sem filtros - busca tudo
      page = historicoTransacaoRepository.findByUsuarioIdOrderByDataTransacaoDesc(
        usuarioId,
        pageable
      );
    }

    List<HistoricoTransacaoResponseDTO> transacoes = page.getContent().stream()
      .map(this::mapToResponseDTO)
      .collect(Collectors.toList());

    // Calcula totais (considerando TODOS os registros, não apenas a página atual)
    Long totalRecebido = historicoTransacaoRepository.somarTokensRecebidos(usuarioId);
    Long totalGasto = Math.abs(historicoTransacaoRepository.somarTokensGastos(usuarioId));

    return new HistoricoTransacaoListResponseDTO(
      transacoes,
      totalRecebido,
      totalGasto,
      usuario.getQntdToken(),
      page.hasNext(),
      page.getTotalPages(),
      page.getTotalElements()
    );
  }

  /**
   * Mapeia HistoricoTransacao para DTO
   */
  private HistoricoTransacaoResponseDTO mapToResponseDTO(HistoricoTransacao historico) {
    return new HistoricoTransacaoResponseDTO(
      historico.getId(),
      historico.getQuantidade(),
      historico.getMotivo(),
      historico.getMotivo().getDescricao(),
      historico.getDescricao(),
      historico.getDataTransacao()
    );
  }
}
