package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioRankingProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRankingDTO {
  private BigInteger id;
  private Long posicao;
  private String nome;
  private Integer qntdXp;
  private Integer nivel;

  public UsuarioRankingDTO(UsuarioRankingProjection usuarioRankingProjection) {
    this.id = usuarioRankingProjection.getId();
    this.posicao = usuarioRankingProjection.getPosicao();
    this.nome = usuarioRankingProjection.getNome();
    this.qntdXp = usuarioRankingProjection.getQntdXp();
    this.nivel = usuarioRankingProjection.getNivel();
  }
}
