package br.com.escoladeti.api_know_hall.entity.workshop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "descricao_workshop")
public class DescricaoWorkshop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @Column(name = "tema", nullable = false, length = 255)
  private String tema;

  @Column(name = "descricao", columnDefinition = "TEXT")
  private String descricao;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_workshop", nullable = false, unique = true)
  private Workshop workshop;
}
