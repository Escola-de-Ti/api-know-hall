package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConquistaProgressoDTO {
  private Conquista conquista;
  private TierConquista maiorTierConquistado;
  private ConquistaTier proximoTier;
  private List<UsuarioConquista> tiersConquistados;

  public boolean isCompleta() {
    return proximoTier == null;
  }

  public double getPercentualProgresso() {
    if (proximoTier == null) return 100.0;

    int progressoAtual = tiersConquistados.isEmpty() ? 0 :
      tiersConquistados.getLast().getProgressoAtual();

    return (progressoAtual * 100.0) / proximoTier.getQuantidadeNecessaria();
  }
}
