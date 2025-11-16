package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDetalhesResponseDTO {

  private String nome;
  private List<Tag> tags;
  private String biografia;
  private Integer nivel;
  private Long xp;
  private Long tokens;
  private Integer qtdPosts;
  private Integer qtdComentarios;
  private Integer qtdUpVotes;
  private Integer qtdSuperVotes;
  private Integer qtdWorkshops;
  private String imagemUrl;
  private Long posicaoRanking;

  private Long xpProximoNivel;

}
