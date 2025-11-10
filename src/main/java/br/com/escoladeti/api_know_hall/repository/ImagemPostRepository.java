package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.ImagemPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface ImagemPostRepository extends JpaRepository<ImagemPost, BigInteger> {

  Optional<ImagemPost> findByImagemId(BigInteger imagem_id);


  void deleteByImagemId(BigInteger imagem_id);
}
