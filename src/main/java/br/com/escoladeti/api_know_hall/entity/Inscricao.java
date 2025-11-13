package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(name = "inscricao",
  uniqueConstraints = @UniqueConstraint(name = "uk_usuario_workshop_inscricao", columnNames = {"id_usuario", "id_workshop"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inscricao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private BigInteger id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_workshop", nullable = false)
  private Workshop workshop;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private StatusInscricao status;

  @CreationTimestamp
  @Column(name = "data_inscricao", nullable = false, updatable = false)
  private Timestamp dataInscricao;
}
