package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class UsuarioService {

  @Autowired
  private UsuarioRepository usuarioRepository;


  public List<Usuario> getAllUsuarios() {
    return usuarioRepository.findAll();
  }

  public Usuario getUsuarioById(BigInteger id) {
    return usuarioRepository.findById(id).orElse(null);
  }

  public Usuario createUsuario(UsuarioCreateDTO usuario) {
    Usuario newUsuario = usuario.toEntity();
    return usuarioRepository.save(newUsuario);
  }

  public Usuario updateUsuario(BigInteger id, UsuarioUpdateDTO usuarioDetails) {
    Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Erro na busca do usuário"));
    usuario = usuarioDetails.toUpdateEntity(usuario);
    return usuarioRepository.save(usuario);
  }

  public void deleteUsuario(BigInteger id) {
    usuarioRepository.deleteById(id);
  }

}
