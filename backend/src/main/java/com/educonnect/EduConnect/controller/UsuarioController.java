package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
public class UsuarioController {
    
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDTO> dtos = usuarios.stream()
            .map(u -> modelMapper.map(u, UsuarioDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return ResponseEntity.ok(modelMapper.map(usuario, UsuarioDTO.class));
    }
    
    @GetMapping("/alunos")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioDTO>> getAlunos() {
        List<Usuario> alunos = usuarioRepository.findAll().stream()
            .filter(u -> u.getRole().name().equals("ALUNO"))
            .collect(Collectors.toList());
        
        List<UsuarioDTO> dtos = alunos.stream()
            .map(u -> {
                UsuarioDTO dto = modelMapper.map(u, UsuarioDTO.class);
                // Adicionar nome do responsável se existir
                if (u.getResponsavel() != null) {
                    dto.setResponsavelNome(u.getResponsavel().getNome());
                }
                return dto;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setTelefone(usuario.getTelefone());
        usuarioExistente.setRole(usuario.getRole());
        usuarioExistente.setAtivo(usuario.getAtivo());
        
        Usuario updated = usuarioRepository.save(usuarioExistente);
        return ResponseEntity.ok(modelMapper.map(updated, UsuarioDTO.class));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> atualizarPerfil(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestBody Map<String, String> dados) {
        
        Usuario usuario = usuarioRepository.findByEmail(usuarioLogado.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Atualizar dados básicos
        if (dados.containsKey("nome")) {
            usuario.setNome(dados.get("nome"));
        }
        
        if (dados.containsKey("email")) {
            // Verificar se o novo email já existe
            String novoEmail = dados.get("email");
            if (!novoEmail.equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(novoEmail)) {
                    throw new RuntimeException("E-mail já está em uso");
                }
                usuario.setEmail(novoEmail);
            }
        }
        
        if (dados.containsKey("telefone")) {
            usuario.setTelefone(dados.get("telefone"));
        }
        
        // Atualizar senha se fornecida
        if (dados.containsKey("password") && dados.get("password") != null && !dados.get("password").isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dados.get("password")));
        }
        
        Usuario updated = usuarioRepository.save(usuario);
        return ResponseEntity.ok(modelMapper.map(updated, UsuarioDTO.class));
    }
}

