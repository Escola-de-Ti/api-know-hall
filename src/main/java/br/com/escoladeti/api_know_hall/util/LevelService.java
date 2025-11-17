package br.com.escoladeti.api_know_hall.util;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelService {

  private final UsuarioRepository usuarioRepository;
  private final HistoricoTransacaoService historicoTransacaoService;

  @Transactional
  public void processLevelChange(Usuario usuario, long oldXp, long newXp) {
    int oldLevel = LevelConfiguration.calculateLevel(oldXp);
    int newLevel = LevelConfiguration.calculateLevel(newXp);

    if (oldLevel == newLevel) {
      if (usuario.getNivel() == null || usuario.getNivel() != newLevel) {
        usuario.setNivel(newLevel);
      }
      return;
    }

    usuario.setNivel(newLevel);

    if (newLevel > oldLevel) {
      processLevelUp(usuario, oldLevel, newLevel);
    } else {
      processLevelDown(usuario, oldLevel, newLevel);
    }
  }

  private void processLevelUp(Usuario usuario, int oldLevel, int newLevel) {
    log.info("Usuário {} subiu de nível: {} -> {}", usuario.getId(), oldLevel, newLevel);

    for (int nivel = oldLevel + 1; nivel <= newLevel; nivel++) {
      concederRecompensasNivel(usuario, nivel);
    }
  }

  private void processLevelDown(Usuario usuario, int oldLevel, int newLevel) {
    log.warn("Usuário {} perdeu nível(is): {} -> {}", usuario.getId(), oldLevel, newLevel);
  }

  private void concederRecompensasNivel(Usuario usuario, int nivel) {
    LevelConfiguration.LevelData levelData = LevelConfiguration.getLevelData(nivel);

    long tokensRecompensa = levelData.getTokensRecompensa();
    String badge = levelData.getBadge();

    if (tokensRecompensa > 0) {
      usuario.setQntdToken(usuario.getQntdToken() + tokensRecompensa);
      usuario.setQntdXp(usuario.getQntdXp() + tokensRecompensa);

      String descricao = String.format(
        "Recompensa por atingir o nível %d%s",
        nivel,
        badge != null ? " - Badge: " + badge : ""
      );

      historicoTransacaoService.registrarTransacao(
        usuario,
        tokensRecompensa,
        MotivoTransacao.LEVEL_UP,
        descricao
      );

      log.info("Usuário {} recebeu {} tokens por atingir nível {}",
        usuario.getId(), tokensRecompensa, nivel);
    }

    if (badge != null) {
      log.info("Usuário {} conquistou o badge '{}' no nível {}",
        usuario.getId(), badge, nivel);
    }
  }

  @Transactional
  public void recalculateLevel(Usuario usuario) {
    long currentXp = usuario.getQntdXp() != null ? usuario.getQntdXp() : 0L;
    int calculatedLevel = LevelConfiguration.calculateLevel(currentXp);

    if (usuario.getNivel() == null || usuario.getNivel() != calculatedLevel) {
      usuario.setNivel(calculatedLevel);
      usuarioRepository.save(usuario);

      log.info("Nível do usuário {} recalculado: {}", usuario.getId(), calculatedLevel);
    }
  }

  public void initializeLevel(Usuario usuario) {
    if (usuario.getQntdXp() == null) {
      usuario.setQntdXp(0L);
    }
    usuario.setNivel(LevelConfiguration.calculateLevel(usuario.getQntdXp()));
  }

  public LevelInfo getLevelInfo(Usuario usuario) {
    long currentXp = usuario.getQntdXp() != null ? usuario.getQntdXp() : 0L;
    int currentLevel = LevelConfiguration.calculateLevel(currentXp);

    return LevelInfo.builder()
      .nivel(currentLevel)
      .xpAtual(currentXp)
      .xpProximoNivel(LevelConfiguration.getXpToNextLevel(currentXp))
      .progressoPercentual(LevelConfiguration.getLevelProgress(currentXp))
      .badge(LevelConfiguration.getLevelBadge(currentLevel))
      .build();
  }

  @lombok.Builder
  @lombok.Getter
  public static class LevelInfo {
    private int nivel;
    private long xpAtual;
    private long xpProximoNivel;
    private double progressoPercentual;
    private String badge;
  }
}
