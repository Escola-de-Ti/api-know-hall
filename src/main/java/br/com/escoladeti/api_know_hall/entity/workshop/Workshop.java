package br.com.escoladeti.api_know_hall.entity.workshop;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WORKSHOP")
public class Workshop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @Column(name = "titulo", nullable = false, length = 255)
  private String titulo;

  @Column(name = "link_meet", length = 500)
  private String linkMeet;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private StatusWorkshop status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_instrutor", nullable = false)
  private Usuario instrutor;

  @OneToOne(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
  private DescricaoWorkshop descricao;

  @CreationTimestamp
  @Column(name = "data_criacao", nullable = false, updatable = false)
  private Timestamp dataCriacao;

  @Column(name = "data_inicio", nullable = false)
  private Timestamp dataInicio;

  @Column(name = "data_termino", nullable = false)
  private Timestamp dataTermino;

  public void setDescricao(DescricaoWorkshop descricao) {
    if (descricao == null) {
      if (this.descricao != null) {
        this.descricao.setWorkshop(null);
      }
    } else {
      descricao.setWorkshop(this);
    }
    this.descricao = descricao;
  }
}
