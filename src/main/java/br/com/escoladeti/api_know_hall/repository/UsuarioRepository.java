package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, BigInteger> {

  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByCpf(String cpf);

}
