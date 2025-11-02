package br.com.escoladeti.api_know_hall.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "IMAGEM")
@AllArgsConstructor
@NoArgsConstructor
@Data
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

}
