package br.com.escoladeti.api_know_hall.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Table(name = "IMAGEM_POST")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImagemPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(cascade = CascadeType.REMOVE)
  @JoinColumn(name = "imagem_id", nullable = false)
  private Imagem imagem;

  @Column(name = "ordem_imagem")
  private Integer ordemImagem;
}
