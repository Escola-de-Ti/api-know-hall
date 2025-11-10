package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.RefreshTokenRequest;
import br.com.escoladeti.api_know_hall.dto.usuario.*;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

  @Autowired
  private UsuarioService usuarioService;

  @GetMapping
  public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
    List<Usuario> usuarios = usuarioService.getAllUsuarios();
    List<UsuarioResponseDTO> response = usuarios.stream()
      .map(UsuarioResponseDTO::new)
      .collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable BigInteger id) {
    Usuario usuario = usuarioService.getUsuarioById(id);
    UsuarioResponseDTO response = new UsuarioResponseDTO(usuario);
    return ResponseEntity.ok(response);
  }

  @PostMapping()
  public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioCreateDTO usuario) {
    Usuario createdUsuario = usuarioService.createUsuario(usuario);
    UsuarioResponseDTO response = new UsuarioResponseDTO(createdUsuario);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/user")
  public ResponseEntity<UsuarioResponseDTO> updateUsuario(
    Principal principal,
    @Valid @RequestBody UsuarioUpdateDTO usuarioDetails) {
    Usuario updatedUsuario = usuarioService.updateUsuario(principal.getName(), usuarioDetails);
    UsuarioResponseDTO response = new UsuarioResponseDTO(updatedUsuario);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUsuario(@PathVariable BigInteger id) {
    usuarioService.deleteUsuario(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/login")
  public ResponseEntity<JwtTokenDTO> login(@Valid @RequestBody UsuarioLoginDTO loginDTO) {
    JwtTokenDTO token = usuarioService.login(loginDTO.email(), loginDTO.senha());
    return ResponseEntity.ok(token);
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtTokenDTO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    JwtTokenDTO token = usuarioService.refreshToken(request.refresh_token());
    return ResponseEntity.ok(token);
  }

  @GetMapping("/ranking")
  public ResponseEntity<RankingResponseDTO> obterRanking(Principal principal) {
    RankingResponseDTO ranking = usuarioService.obterRanking(principal.getName());
    return ResponseEntity.ok(ranking);
  }

}
