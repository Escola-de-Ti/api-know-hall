package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
  private List<Tag> tags = new ArrayList<>();
}
