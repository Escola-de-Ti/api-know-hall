package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.exception.*;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import br.com.escoladeti.api_know_hall.util.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class UsuarioService {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private JwtTokenService jwtTokenService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private CpfValidator cpfValidator;

  @Autowired
  private EmailValidator emailValidator;

  @Autowired
  private TelefoneValidator telefoneValidator;

  @Autowired
  private NomeValidator nomeValidator;

  @Autowired
  private SenhaValidator senhaValidator;

  @Autowired
  private PalavrasProibidasService palavrasProibidasService;

  public List<Usuario> getAllUsuarios() {
    return usuarioRepository.findAll();
  }

  public Usuario getUsuarioById(BigInteger id) {
    return usuarioRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
  }

  @Transactional
  public Usuario createUsuario(UsuarioCreateDTO dto) {
    if (!cpfValidator.isValid(dto.getCpf())) {
      throw new ValidationException("CPF inválido");
    }

    if (!emailValidator.isValid(dto.getEmail())) {
      throw new ValidationException("Formato de email inválido");
    }

    if (!nomeValidator.isValid(dto.getNome())) {
      throw new ValidationException("Nome inválido. Deve conter apenas letras e ter entre 2 e 100 caracteres");
    }

    if (palavrasProibidasService.contemPalavraProibida(dto.getNome())) {
      String palavraEncontrada = palavrasProibidasService.identificarPalavraProibida(dto.getNome());
      throw new ValidationException("Nome contém conteúdo não permitido: " + palavraEncontrada);
    }

    if (dto.getBiografia() != null && !dto.getBiografia().isBlank()) {
      if (palavrasProibidasService.contemPalavraProibida(dto.getBiografia())) {
        String palavraEncontrada = palavrasProibidasService.identificarPalavraProibida(dto.getBiografia());
        throw new ValidationException("Biografia contém conteúdo não permitido: " + palavraEncontrada);
      }
    }

    if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
      if (!telefoneValidator.isValid(dto.getTelefone())) {
        throw new ValidationException("Formato de telefone inválido");
      }
    }

    if (dto.getTelefone2() != null && !dto.getTelefone2().isBlank()) {
      if (!telefoneValidator.isValid(dto.getTelefone2())) {
        throw new ValidationException("Formato de telefone 2 inválido");
      }
    }

    if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
      throw new DuplicateResourceException("Email já cadastrado no sistema");
    }

    if (usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
      throw new DuplicateResourceException("CPF já cadastrado no sistema");
    }

    if (!senhaValidator.isValid(dto.getSenha())) {
      String mensagemErro = senhaValidator.getMensagemErro(dto.getSenha());
      throw new ValidationException(mensagemErro);
    }

    String senhaCriptografada = passwordEncoder.encode(dto.getSenha());
    dto.setSenha(senhaCriptografada);

    if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
      dto.setTelefone(telefoneValidator.formatarTelefone(dto.getTelefone()));
    }
    if (dto.getTelefone2() != null && !dto.getTelefone2().isBlank()) {
      dto.setTelefone2(telefoneValidator.formatarTelefone(dto.getTelefone2()));
    }

    Usuario newUsuario = new Usuario(dto);
    return usuarioRepository.save(newUsuario);
  }

  @Transactional
  public Usuario updateUsuario(String email, UsuarioUpdateDTO dto) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
      if (!emailValidator.isValid(dto.getEmail())) {
        throw new ValidationException("Formato de email inválido");
      }

      if (!dto.getEmail().equals(usuario.getEmail()) &&
        usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
        throw new DuplicateResourceException("Email já cadastrado no sistema");
      }
    }

    if (dto.getCpf() != null && !dto.getCpf().isBlank()) {
      if (!cpfValidator.isValid(dto.getCpf())) {
        throw new ValidationException("CPF inválido");
      }

      if (!dto.getCpf().equals(usuario.getCpf()) &&
        usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
        throw new DuplicateResourceException("CPF já cadastrado no sistema");
      }
    }

    if (dto.getNome() != null && !dto.getNome().isBlank()) {
      if (!nomeValidator.isValid(dto.getNome())) {
        throw new ValidationException("Nome inválido. Deve conter apenas letras e ter entre 2 e 100 caracteres");
      }
      if (palavrasProibidasService.contemPalavraProibida(dto.getNome())) {
        String palavraEncontrada = palavrasProibidasService.identificarPalavraProibida(dto.getNome());
        throw new ValidationException("Nome contém conteúdo não permitido: " + palavraEncontrada);
      }
    }

    if (dto.getBiografia() != null && !dto.getBiografia().isBlank()) {
      if (palavrasProibidasService.contemPalavraProibida(dto.getBiografia())) {
        String palavraEncontrada = palavrasProibidasService.identificarPalavraProibida(dto.getBiografia());
        throw new ValidationException("Biografia contém conteúdo não permitido: " + palavraEncontrada);
      }
    }

    if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
      if (!telefoneValidator.isValid(dto.getTelefone())) {
        throw new ValidationException("Formato de telefone inválido");
      }
    }

    if (dto.getTelefone2() != null && !dto.getTelefone2().isBlank()) {
      if (!telefoneValidator.isValid(dto.getTelefone2())) {
        throw new ValidationException("Formato de telefone 2 inválido");
      }
    }

    if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
      if (!senhaValidator.isValid(dto.getSenha())) {
        String mensagemErro = senhaValidator.getMensagemErro(dto.getSenha());
        throw new ValidationException(mensagemErro);
      }
      dto.setSenha(passwordEncoder.encode(dto.getSenha()));
    }

    if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
      dto.setTelefone(telefoneValidator.formatarTelefone(dto.getTelefone()));
    }
    if (dto.getTelefone2() != null && !dto.getTelefone2().isBlank()) {
      dto.setTelefone2(telefoneValidator.formatarTelefone(dto.getTelefone2()));
    }

    usuario.applyUpdate(dto);
    return usuarioRepository.save(usuario);
  }

  @Transactional
  public void deleteUsuario(BigInteger id) {
    if (!usuarioRepository.existsById(id)) {
      throw new EntityNotFoundException("Usuário não encontrado");
    }
    usuarioRepository.deleteById(id);
  }

  public JwtTokenDTO login(String email, String senha) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new InvalidCredentialsException("Email ou senha inválidos"));

    if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
      throw new InvalidCredentialsException("Email ou senha inválidos");
    }

    if (!usuario.getStatusUsuario().equals(StatusUsuario.ATIVO)) {
      throw new UsuarioInativoException("Usuário inativo. Entre em contato com o suporte.");
    }

    return jwtTokenService.generateTokenWithExpiration(usuario.getEmail());
  }

  public JwtTokenDTO refreshToken(String refreshToken) {
    return jwtTokenService.refreshTokens(refreshToken);
  }

  @Transactional
  public void atualizarImagemPerfil(String email, Imagem imagem) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    usuario.setImagemPerfil(imagem);
    usuarioRepository.save(usuario);
  }
}
