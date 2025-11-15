package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

  private BigInteger id;
  private String email;
  private String nome;
  private String biografia;
  private String telefone;
  private String telefone2;
  private StatusUsuario statusUsuario;
  private TipoUsuario tipoUsuario;
  private Long qntdToken;
  private Long qntdXp;
  private BigInteger idImagemPerfil;
  private String urlImagemPerfil;
  private List<String> tags;

  public UsuarioResponseDTO(Usuario usuario) {
    this.id = usuario.getId();
    this.email = usuario.getEmail();
    this.nome = usuario.getNome();
    this.biografia = usuario.getBiografia();
    this.telefone = usuario.getTelefone();
    this.telefone2 = usuario.getTelefone2();
    this.statusUsuario = usuario.getStatusUsuario();
    this.tipoUsuario = usuario.getTipoUsuario();
    this.qntdToken = usuario.getQntdToken();
    this.qntdXp = usuario.getQntdXp();
    this.idImagemPerfil = usuario.getImagemPerfil() != null ? usuario.getImagemPerfil().getId() : null;
    this.tags = usuario.getTags() != null
      ? usuario.getTags().stream().map(Tag::getName).collect(Collectors.toList())
      : List.of();
    this.urlImagemPerfil = usuario.getImagemPerfil() != null ? usuario.getImagemPerfil().getUrl() : null;
  }
}
