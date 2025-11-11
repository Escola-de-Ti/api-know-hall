package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.HistoricoTransacao;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;

@Repository
public interface HistoricoTransacaoRepository extends JpaRepository<HistoricoTransacao, BigInteger> {

  Page<HistoricoTransacao> findByUsuarioIdOrderByDataTransacaoDesc(
    BigInteger usuarioId,
    Pageable pageable
  );

  Page<HistoricoTransacao> findByUsuarioIdAndMotivo(
    BigInteger usuarioId,
    MotivoTransacao motivo,
    Pageable pageable
  );

  Page<HistoricoTransacao> findByUsuarioIdAndDataTransacaoBetween(
    BigInteger usuarioId,
    Timestamp dataInicio,
    Timestamp dataFim,
    Pageable pageable
  );

  @Query("SELECT h FROM HistoricoTransacao h " +
    "WHERE h.usuario.id = :usuarioId " +
    "AND h.motivo = :motivo " +
    "AND h.dataTransacao BETWEEN :dataInicio AND :dataFim " +
    "ORDER BY h.dataTransacao DESC")
  Page<HistoricoTransacao> findByUsuarioIdAndMotivoAndPeriodo(
    @Param("usuarioId") BigInteger usuarioId,
    @Param("motivo") MotivoTransacao motivo,
    @Param("dataInicio") Timestamp dataInicio,
    @Param("dataFim") Timestamp dataFim,
    Pageable pageable
  );

  @Query("SELECT COALESCE(SUM(h.quantidade), 0) FROM HistoricoTransacao h " +
    "WHERE h.usuario.id = :usuarioId AND h.quantidade > 0")
  Long somarTokensRecebidos(@Param("usuarioId") BigInteger usuarioId);

  @Query("SELECT COALESCE(SUM(h.quantidade), 0) FROM HistoricoTransacao h " +
    "WHERE h.usuario.id = :usuarioId AND h.quantidade < 0")
  Long somarTokensGastos(@Param("usuarioId") BigInteger usuarioId);
}
