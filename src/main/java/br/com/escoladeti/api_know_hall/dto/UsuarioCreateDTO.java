package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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

  private List<Tag> tags = new ArrayList<>();

  public UsuarioCreateDTO() {
  }

  public UsuarioCreateDTO(String email, String cpf, String telefone, String telefone2, String nome, String biografia, String senha, Integer idImagemPerfil, TipoUsuario tipoUsuario, List<Tag> tags) {
    this.email = email;
    this.cpf = cpf;
    this.telefone = telefone;
    this.telefone2 = telefone2;
    this.nome = nome;
    this.biografia = biografia;
    this.senha = senha;
    this.idImagemPerfil = idImagemPerfil;
    this.tipoUsuario = tipoUsuario;
    this.tags = tags;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public String getTelefone2() {
    return telefone2;
  }

  public void setTelefone2(String telefone2) {
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

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
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

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }
}
