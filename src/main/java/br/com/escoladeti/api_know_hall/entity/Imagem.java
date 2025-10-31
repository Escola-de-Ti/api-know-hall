package br.com.escoladeti.api_know_hall.entity;

import jakarta.persistence.*;

import java.math.BigInteger;

@Entity
@Table(name = "IMAGEM")
public class Imagem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "id_imagem")
  private String idImagem;

  @Column(name = "path", nullable = false)
  private String path;

  public Imagem() {
  }

  public Imagem(BigInteger id, String nome, String url, String idImagem, String path) {
    this.id = id;
    this.nome = nome;
    this.url = url;
    this.idImagem = idImagem;
    this.path = path;
  }

  public BigInteger getId() {
    return id;
  }

  public void setId(BigInteger id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getIdImagem() {
    return idImagem;
  }

  public void setIdImagem(String idImagem) {
    this.idImagem = idImagem;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
