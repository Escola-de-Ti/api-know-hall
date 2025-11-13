package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, BigInteger> {

  @Query(value = """
    SELECT w.*
    FROM workshop w
    WHERE w.id_instrutor = :instrutorId
    ORDER BY w.id DESC
    """, nativeQuery = true)
  List<Workshop> findByInstrutorId(@Param("instrutorId") Long instrutorId);

  @Query(value = """
    SELECT w.*
    FROM workshop w
    WHERE w.status = :status
    ORDER BY w.id DESC
    """, nativeQuery = true)
  List<Workshop> findByStatus(@Param("status") String status);

  @Query(value = """
    SELECT w.*
    FROM workshop w
    LEFT JOIN descricao_workshop dw ON w.id = dw.id_workshop
    WHERE w.id = :workshopId
    """, nativeQuery = true)
  Optional<Workshop> findByIdWithDescricao(@Param("workshopId") Long workshopId);

  @Query(value = """
    SELECT w.*
    FROM workshop w
    WHERE w.status = 'ABERTO'
    ORDER BY w.id DESC
    """, nativeQuery = true)
  List<Workshop> findWorkshopsAbertos();

  @Query(value = """
    SELECT COUNT(*)
    FROM workshop w
    WHERE w.id_instrutor = :instrutorId
    """, nativeQuery = true)
  Long countByInstrutorId(@Param("instrutorId") Long instrutorId);

  @Query(value = """
    SELECT w.*
    FROM workshop w
    WHERE w.id_instrutor = :instrutorId
      AND w.status = :status
    ORDER BY w.id DESC
    """, nativeQuery = true)
  List<Workshop> findByInstrutorIdAndStatus(
    @Param("instrutorId") Long instrutorId,
    @Param("status") String status
  );

  @Query(value = """
    SELECT w.*
    FROM workshop w
    WHERE LOWER(w.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
    ORDER BY w.id DESC
    """, nativeQuery = true)
  List<Workshop> findByTituloContaining(@Param("termo") String termo);

}
