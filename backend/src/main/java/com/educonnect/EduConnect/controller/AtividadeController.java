package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.AtividadeCreateDTO;
import com.educonnect.EduConnect.dto.AtividadeDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.AtividadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AtividadeController {
    
    private final AtividadeService atividadeService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AtividadeDTO>> listarTodas(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(name = "escolaId", required = false) Long escolaId,
            @RequestParam(name = "professorId", required = false) Long professorId) {
        return ResponseEntity.ok(atividadeService.listarTodas(usuarioLogado, escolaId, professorId));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AtividadeDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(atividadeService.buscarPorId(id, usuarioLogado));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<AtividadeDTO> criar(
            @Valid @RequestBody AtividadeCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        AtividadeDTO atividade = atividadeService.criar(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(atividade);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<AtividadeDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtividadeCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(atividadeService.atualizar(id, dto, usuarioLogado));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        atividadeService.deletar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}

