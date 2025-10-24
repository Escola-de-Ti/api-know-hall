package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaProgressoDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.repository.ConquistaRepository;
import br.com.escoladeti.api_know_hall.repository.ConquistaTierRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioConquistaRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ConquistaService {

  private final ConquistaRepository conquistaRepository;
  private final ConquistaTierRepository conquistaTierRepository;
  private final UsuarioConquistaRepository usuarioConquistaRepository;
  private final UsuarioRepository usuarioRepository;

  public ConquistaService(ConquistaRepository conquistaRepository,
                          ConquistaTierRepository conquistaTierRepository,
                          UsuarioConquistaRepository usuarioConquistaRepository,
                          UsuarioRepository usuarioRepository) {
    this.conquistaRepository = conquistaRepository;
    this.conquistaTierRepository = conquistaTierRepository;
    this.usuarioConquistaRepository = usuarioConquistaRepository;
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Verifica progresso do usuário e concede conquistas automaticamente
   */
  public void verificarEConcederConquistas(BigInteger usuarioId, String campoValidacao, Integer progressoAtual) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    // Busca todas as conquistas relacionadas ao campo de validação
    List<Conquista> conquistas = conquistaRepository.findByCampoValidacao(campoValidacao);

    for (Conquista conquista : conquistas) {
      // Para cada tier da conquista
      for (ConquistaTier tier : conquista.getTiers()) {
        // Verifica se o usuário atingiu a quantidade necessária
        if (progressoAtual >= tier.getQuantidadeNecessaria()) {
          // Verifica se já não possui este tier
          if (!usuarioConquistaRepository.existsByUsuarioIdAndConquistaTierId(usuarioId, tier.getId())) {
            concederConquistaTier(usuario, tier, progressoAtual);
          }
        }
      }
    }
  }

  /**
   * Concede um tier específico de uma conquista ao usuário
   */
  public void concederConquistaTier(Usuario usuario, ConquistaTier tier, Integer progressoAtual) {
    UsuarioConquista uc = new UsuarioConquista();
    uc.setUsuario(usuario);
    uc.setConquistaTier(tier);
    uc.setProgressoAtual(progressoAtual);
    uc.setDataObtencao(LocalDateTime.now());

    usuarioConquistaRepository.save(uc);

    // Aqui você pode adicionar lógica para notificar o usuário, enviar email, etc.
    System.out.println("🏆 " + usuario.getNome() + " conquistou: " +
      tier.getConquista().getNome() + " - " + tier.getTier().name());
  }

  /**
   * Lista todas as conquistas de um usuário com seus tiers
   */
  public List<UsuarioConquista> listarConquistasUsuario(BigInteger usuarioId) {
    return usuarioConquistaRepository.findByUsuarioIdWithDetails(usuarioId);
  }

  /**
   * Obtém o progresso do usuário em uma conquista específica
   */
  public ConquistaProgressoDTO obterProgressoConquista(BigInteger usuarioId, BigInteger conquistaId) {
    Conquista conquista = conquistaRepository.findByIdWithTiers(conquistaId)
      .orElseThrow(() -> new EntityNotFoundException("Conquista não encontrada"));

    List<UsuarioConquista> tiersConquistados = usuarioConquistaRepository
      .findByUsuarioIdAndConquistaId(usuarioId, conquistaId);

    TierConquista maiorTierConquistado = tiersConquistados.stream()
      .map(uc -> uc.getConquistaTier().getTier())
      .max(Comparator.comparingInt(TierConquista::getNivel))
      .orElse(null);

    // Próximo tier a conquistar
    ConquistaTier proximoTier = conquista.getTiers().stream()
      .filter(tier -> maiorTierConquistado == null ||
        tier.getTier().getNivel() > maiorTierConquistado.getNivel())
      .findFirst()
      .orElse(null);

    return new ConquistaProgressoDTO(
      conquista,
      maiorTierConquistado,
      proximoTier,
      tiersConquistados
    );
  }

  /**
   * Cria uma conquista com múltiplos tiers
   */
  public Conquista criarConquistaComTiers(String nome, String descricao,
                                          String campoValidacao, TipoConquista tipo,
                                          Map<TierConquista, Integer> tiersComQuantidades) {
    Conquista conquista = new Conquista();
    conquista.setNome(nome);
    conquista.setDescricao(descricao);
    conquista.setCampoValidacao(campoValidacao);
    conquista.setTipoConquista(tipo);

    conquista = conquistaRepository.save(conquista);

    // Adiciona os tiers
    for (Map.Entry<TierConquista, Integer> entry : tiersComQuantidades.entrySet()) {
      ConquistaTier tier = new ConquistaTier();
      tier.setConquista(conquista);
      tier.setTier(entry.getKey());
      tier.setQuantidadeNecessaria(entry.getValue());
      conquistaTierRepository.save(tier);
    }

    // ADICIONAR estas linhas:
    return conquistaRepository.findByIdWithTiers(conquista.getId())
      .orElse(conquista);
  }

  @Transactional(readOnly = true)
  public List<Conquista> listarConquistasPorTipo(TipoConquista tipo) {
    return conquistaRepository.findByTipoConquista(tipo);
  }

  @Transactional(readOnly = true)
  public List<Conquista> listarTodasConquistas() {
    return conquistaRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Conquista buscarConquistaPorId(BigInteger id) {
    return conquistaRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Conquista não encontrada com ID: " + id));
  }

  @Transactional(readOnly = true)
  public List<Conquista> listarConquistasPorCampo(String campoValidacao) {
    return conquistaRepository.findByCampoValidacao(campoValidacao);
  }

  @Transactional(readOnly = true)
  public List<UsuarioConquista> listarConquistasUsuarioPorTipo(BigInteger usuarioId, TipoConquista tipo) {
    return usuarioConquistaRepository.findByUsuarioIdAndTipo(usuarioId, tipo.name());
  }
}
