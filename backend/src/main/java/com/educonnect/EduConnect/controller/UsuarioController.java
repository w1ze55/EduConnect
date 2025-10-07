package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.UsuarioCreateDTO;
import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import com.educonnect.EduConnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios(
            @AuthenticationPrincipal Usuario usuarioLogado) {
        List<Usuario> usuarios;
        
        // Se for ADMINISTRADOR, vê todos os usuários
        if (usuarioLogado.getRole().name().equals("ADMINISTRADOR")) {
            usuarios = usuarioRepository.findAll();
        } 
        // Se for DIRETORIA, vê apenas usuários da sua escola
        else if (usuarioLogado.getRole().name().equals("DIRETORIA")) {
            if (usuarioLogado.getEscola() == null) {
                throw new RuntimeException("Diretor não está vinculado a nenhuma escola");
            }
            usuarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getEscola() != null && 
                           u.getEscola().getId().equals(usuarioLogado.getEscola().getId()))
                .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Acesso não autorizado");
        }
        
        List<UsuarioDTO> dtos = usuarios.stream()
            .map(u -> {
                UsuarioDTO dto = modelMapper.map(u, UsuarioDTO.class);
                if (u.getEscola() != null) {
                    dto.setEscolaId(u.getEscola().getId());
                    dto.setEscolaNome(u.getEscola().getNome());
                }
                if (u.getResponsavel() != null) {
                    dto.setResponsavelId(u.getResponsavel().getId());
                    dto.setResponsavelNome(u.getResponsavel().getNome());
                }
                return dto;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
        if (usuario.getEscola() != null) {
            dto.setEscolaId(usuario.getEscola().getId());
            dto.setEscolaNome(usuario.getEscola().getNome());
        }
        if (usuario.getResponsavel() != null) {
            dto.setResponsavelId(usuario.getResponsavel().getId());
            dto.setResponsavelNome(usuario.getResponsavel().getNome());
        }
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioDTO usuario = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
    
    @GetMapping("/diretores-disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DIRETORIA')")
    public ResponseEntity<List<UsuarioDTO>> listarDiretoresDisponiveis() {
        return ResponseEntity.ok(usuarioService.listarDiretoresDisponiveis());
    }
    
    @GetMapping("/responsaveis-disponiveis")
    public ResponseEntity<List<UsuarioDTO>> listarResponsaveisDisponiveis(@RequestParam(required = false) Long escolaId) {
        return ResponseEntity.ok(usuarioService.listarResponsaveisDisponiveis(escolaId));
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
                    dto.setResponsavelId(u.getResponsavel().getId());
                    dto.setResponsavelNome(u.getResponsavel().getNome());
                }
                // Adicionar escola se existir
                if (u.getEscola() != null) {
                    dto.setEscolaId(u.getEscola().getId());
                    dto.setEscolaNome(u.getEscola().getNome());
                }
                return dto;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
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

