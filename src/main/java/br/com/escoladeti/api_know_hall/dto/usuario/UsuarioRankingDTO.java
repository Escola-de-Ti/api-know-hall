package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioRankingProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRankingDTO {
  private Long posicao;
  private String nome;
  private Integer qntdXp;
  private Integer nivel;

  public UsuarioRankingDTO(UsuarioRankingProjection usuarioRankingProjection) {
    this.posicao = usuarioRankingProjection.getPosicao();
    this.nome = usuarioRankingProjection.getNome();
    this.qntdXp = usuarioRankingProjection.getQntdXp();
    this.nivel = usuarioRankingProjection.getNivel();
  }
}
