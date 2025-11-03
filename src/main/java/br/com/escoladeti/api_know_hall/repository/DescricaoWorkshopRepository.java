package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.workshop.DescricaoWorkshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface DescricaoWorkshopRepository extends JpaRepository<DescricaoWorkshop, BigInteger> {

  @Query(value = """
        SELECT dw.*
        FROM descricao_workshop dw
        WHERE dw.id_workshop = :workshopId
        """, nativeQuery = true)
  Optional<DescricaoWorkshop> findByWorkshopId(@Param("workshopId") BigInteger workshopId);

  @Query(value = """
        SELECT EXISTS(
            SELECT 1
            FROM descricao_workshop dw
            WHERE dw.id_workshop = :workshopId
        )
        """, nativeQuery = true)
  Boolean existsByWorkshopId(@Param("workshopId") BigInteger workshopId);
}
