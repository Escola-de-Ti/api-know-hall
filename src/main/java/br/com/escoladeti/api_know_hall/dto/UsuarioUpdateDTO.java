package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioUpdateDTO {

  @Email(message = "Email deve ter um formato válido")
  private String email;

  @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
  private String cpf;

  @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
  private String telefone;

  @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
  private String telefone2;
  private String nome;
  private String biografia;

  @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
  private String senha;
  private Integer idImagemPerfil;
  private TipoUsuario tipoUsuario;
  private StatusUsuario statusUsuario;

    public Usuario toUpdateEntity(Usuario usuario) {
        return new Usuario(
            usuario.getIdUsuario(),
            this.email != null ? this.email : usuario.getEmail(),
            this.cpf != null ? this.cpf : usuario.getCpf(),
            this.telefone != null ? this.telefone : usuario.getTelefone(),
            this.telefone2 != null ? this.telefone2 : usuario.getTelefone2(),
            this.nome != null ? this.nome : usuario.getNome(),
            this.biografia != null ? this.biografia : usuario.getBiografia(),
            this.senha != null ? this.senha : usuario.getSenhaHash(),
            this.idImagemPerfil != null ? this.idImagemPerfil : usuario.getIdImagemPerfil(),
            this.statusUsuario != null ? this.statusUsuario : usuario.getStatusUsuario(),
            this.tipoUsuario != null ? this.tipoUsuario : usuario.getTipoUsuario()
        );
    }
}
