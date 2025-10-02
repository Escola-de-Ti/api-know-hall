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
public class UsuarioCreateDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
    private String cpf;

    @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
    private String telefone;

    @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
    private String telefone2;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String biografia;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    private Integer idImagemPerfil;

    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipoUsuario;

  public Usuario toEntity() {
    return new Usuario(
        null,
        this.email,
        this.cpf,
        this.telefone,
        this.telefone2,
        this.nome,
        this.biografia,
        this.senha,
        this.idImagemPerfil,
        StatusUsuario.CONFIRMACAO_PENDENTE,
        this.tipoUsuario
    );
  }
}
