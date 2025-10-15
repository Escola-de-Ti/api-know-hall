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

  public UsuarioUpdateDTO() {
  }

  public UsuarioUpdateDTO(List<Tag> tags, StatusUsuario statusUsuario, TipoUsuario tipoUsuario, Integer idImagemPerfil, String senha, String biografia, String nome, String telefone2, String telefone, String cpf, String email) {
    this.tags = tags;
    this.statusUsuario = statusUsuario;
    this.tipoUsuario = tipoUsuario;
    this.idImagemPerfil = idImagemPerfil;
    this.senha = senha;
    this.biografia = biografia;
    this.nome = nome;
    this.telefone2 = telefone2;
    this.telefone = telefone;
    this.cpf = cpf;
    this.email = email;
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

  public StatusUsuario getStatusUsuario() {
    return statusUsuario;
  }

  public void setStatusUsuario(StatusUsuario statusUsuario) {
    this.statusUsuario = statusUsuario;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }
}
