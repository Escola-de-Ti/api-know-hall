package br.com.escoladeti.api_know_hall.dto.usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLogadoRankingDTO {
  private Long posicao;
  private Integer xpRecebidoUltimos30Dias;
}
