package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;


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

  public UsuarioUpdateDTO(String email, String cpf, String telefone, String telefone2, String nome, String biografia, String senha, Integer idImagemPerfil, TipoUsuario tipoUsuario, StatusUsuario statusUsuario) {
    this.email = email;
    this.cpf = cpf;
    this.telefone = telefone;
    this.telefone2 = telefone2;
    this.nome = nome;
    this.biografia = biografia;
    this.senha = senha;
    this.idImagemPerfil = idImagemPerfil;
    this.tipoUsuario = tipoUsuario;
    this.statusUsuario = statusUsuario;
  }

  public UsuarioUpdateDTO() {
  }

  public @Email(message = "Email deve ter um formato válido") String getEmail() {
    return email;
  }

  public void setEmail(@Email(message = "Email deve ter um formato válido") String email) {
    this.email = email;
  }

  public @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres") String getCpf() {
    return cpf;
  }

  public void setCpf(@Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres") String cpf) {
    this.cpf = cpf;
  }

  public @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres") String getTelefone() {
    return telefone;
  }

  public void setTelefone(@Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres") String telefone) {
    this.telefone = telefone;
  }

  public @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres") String getTelefone2() {
    return telefone2;
  }

  public void setTelefone2(@Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres") String telefone2) {
    this.telefone2 = telefone2;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getBiografia() {
    return biografia;
  }

  public void setBiografia(String biografia) {
    this.biografia = biografia;
  }

  public @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres") String getSenha() {
    return senha;
  }

  public void setSenha(@Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres") String senha) {
    this.senha = senha;
  }

  public Integer getIdImagemPerfil() {
    return idImagemPerfil;
  }

  public void setIdImagemPerfil(Integer idImagemPerfil) {
    this.idImagemPerfil = idImagemPerfil;
  }

  public TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(TipoUsuario tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }

  public StatusUsuario getStatusUsuario() {
    return statusUsuario;
  }

  public void setStatusUsuario(StatusUsuario statusUsuario) {
    this.statusUsuario = statusUsuario;
  }

  public Usuario toUpdateEntity(Usuario usuario) {
        return new Usuario(
            usuario.getId(),
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
