package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConquistaTierRepository extends JpaRepository<ConquistaTier, BigInteger> {

  List<ConquistaTier> findByConquistaId(BigInteger conquistaId);

  Optional<ConquistaTier> findByConquistaIdAndTier(BigInteger conquistaId, TierConquista tier);

  @Query(nativeQuery = true,
          value = "SELECT * FROM CONQUISTA_TIER " +
                  "WHERE conquista_id = :conquistaId " +
                  "ORDER BY CASE tier " +
                  " WHEN 'BRONZE' THEN 1 " +
                  " WHEN 'PRATA' THEN 2 " +
                  " WHEN 'OURO' THEN 3 " +
                  " WHEN 'PLATINA' THEN 4 " +
                  " WHEN 'DIAMANTE' THEN 5 " +
                  "END ASC")
  List<ConquistaTier> findByConquistaIdOrderByTier(@Param("conquistaId") BigInteger conquistaId);
}
