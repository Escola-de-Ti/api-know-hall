package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;

import java.math.BigInteger;
import java.util.List;

@Service
public class UsuarioService {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private JwtTokenService jwtTokenService;


  public List<Usuario> getAllUsuarios() {
    return usuarioRepository.findAll();
  }

  public Usuario getUsuarioById(BigInteger id) {
    return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
  }


  public Usuario createUsuario(UsuarioCreateDTO usuario) {
    Usuario newUsuario = new Usuario(usuario);
    return usuarioRepository.save(newUsuario);
  }

  public Usuario updateUsuario(String email, UsuarioUpdateDTO usuarioDetails) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    usuario.applyUpdate(usuarioDetails);
    return usuarioRepository.save(usuario);
  }

  public void deleteUsuario(BigInteger id) {
    usuarioRepository.deleteById(id);
  }

  public JwtTokenDTO login(String email, String senha) {
    Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));

    if (!usuario.getStatusUsuario().equals(StatusUsuario.ATIVO)) {
      throw new UsuarioInativoException("Usuario inativo");
    }
    return jwtTokenService.generateTokenWithExpiration(usuario.getEmail());
  }

  public JwtTokenDTO refreshToken(String refreshToken) {
    return jwtTokenService.refreshTokens(refreshToken);
  }

  public void atualizarImagemPerfil(String email, Imagem imagem) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    usuario.setImagemPerfil(imagem);
    usuarioRepository.save(usuario);
  }
}
