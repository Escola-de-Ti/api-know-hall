package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.RefreshTokenRequest;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioLoginDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

  @Autowired
  private UsuarioService usuarioService;

  @GetMapping
  public ResponseEntity<List<Usuario>> getAllUsuarios() {
    List<Usuario> usuarios = usuarioService.getAllUsuarios();
    return ResponseEntity.ok(usuarios);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Usuario> getUsuarioById(@PathVariable BigInteger id) {
    Usuario usuario = usuarioService.getUsuarioById(id);
    return ResponseEntity.ok(usuario);
  }

  @PostMapping()
  public ResponseEntity<Usuario> createUsuario(@RequestBody UsuarioCreateDTO usuario) {
    Usuario createdUsuario = usuarioService.createUsuario(usuario);
    return ResponseEntity.status(201).body(createdUsuario);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Usuario> updateUsuario(@PathVariable BigInteger id, @RequestBody UsuarioUpdateDTO usuarioDetails) {
    Usuario updatedUsuario = usuarioService.updateUsuario(id, usuarioDetails);
    return ResponseEntity.ok(updatedUsuario);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUsuario(@PathVariable BigInteger id) {
    usuarioService.deleteUsuario(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/login")
  public ResponseEntity<JwtTokenDTO> login(@RequestBody UsuarioLoginDTO loginDTO) {
    JwtTokenDTO token = usuarioService.login(loginDTO.email(), loginDTO.senha());
    return ResponseEntity.ok(token);
  }

  // Nova rota para recarregar tokens a partir do refresh token
  @PostMapping("/refresh")
  public ResponseEntity<JwtTokenDTO> refresh(@RequestBody RefreshTokenRequest request) {
    JwtTokenDTO token = usuarioService.refreshToken(request.refresh_token());
    return ResponseEntity.ok(token);
  }
}
