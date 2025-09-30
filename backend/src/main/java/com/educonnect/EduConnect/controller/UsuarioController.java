package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {
    
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    
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
}

