package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



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

  public UsuarioCreateDTO(String email, String cpf, String telefone, String telefone2, String nome, String biografia, String senha, Integer idImagemPerfil, TipoUsuario tipoUsuario) {
    this.email = email;
    this.cpf = cpf;
    this.telefone = telefone;
    this.telefone2 = telefone2;
    this.nome = nome;
    this.biografia = biografia;
    this.senha = senha;
    this.idImagemPerfil = idImagemPerfil;
    this.tipoUsuario = tipoUsuario;
  }

  public UsuarioCreateDTO() {
  }

  public @NotBlank(message = "Email é obrigatório") @Email(message = "Email deve ter um formato válido") String getEmail() {
    return email;
  }

  public void setEmail(@NotBlank(message = "Email é obrigatório") @Email(message = "Email deve ter um formato válido") String email) {
    this.email = email;
  }

  public @NotBlank(message = "CPF é obrigatório") @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres") String getCpf() {
    return cpf;
  }

  public void setCpf(@NotBlank(message = "CPF é obrigatório") @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres") String cpf) {
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

  public @NotBlank(message = "Nome é obrigatório") String getNome() {
    return nome;
  }

  public void setNome(@NotBlank(message = "Nome é obrigatório") String nome) {
    this.nome = nome;
  }

  public String getBiografia() {
    return biografia;
  }

  public void setBiografia(String biografia) {
    this.biografia = biografia;
  }

  public @NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres") String getSenha() {
    return senha;
  }

  public void setSenha(@NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres") String senha) {
    this.senha = senha;
  }

  public Integer getIdImagemPerfil() {
    return idImagemPerfil;
  }

  public void setIdImagemPerfil(Integer idImagemPerfil) {
    this.idImagemPerfil = idImagemPerfil;
  }

  public @NotNull(message = "Tipo de usuário é obrigatório") TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(@NotNull(message = "Tipo de usuário é obrigatório") TipoUsuario tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }

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
