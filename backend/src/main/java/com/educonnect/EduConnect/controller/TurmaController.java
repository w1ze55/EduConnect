package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.TurmaCreateDTO;
import com.educonnect.EduConnect.dto.TurmaDTO;
import com.educonnect.EduConnect.dto.TurmaUpdateDTO;
import com.educonnect.EduConnect.service.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TurmaController {
    
    private final TurmaService turmaService;
    
    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listarTodas() {
        return ResponseEntity.ok(turmaService.listarTodas());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(turmaService.buscarPorId(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> criar(@Valid @RequestBody TurmaCreateDTO dto) {
        TurmaDTO turma = turmaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(turma);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TurmaUpdateDTO dto) {
        return ResponseEntity.ok(turmaService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        turmaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{turmaId}/professores/{professorId}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> adicionarProfessor(
            @PathVariable Long turmaId,
            @PathVariable Long professorId) {
        return ResponseEntity.ok(turmaService.adicionarProfessor(turmaId, professorId));
    }
    
    @DeleteMapping("/{turmaId}/professores/{professorId}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> removerProfessor(
            @PathVariable Long turmaId,
            @PathVariable Long professorId) {
        return ResponseEntity.ok(turmaService.removerProfessor(turmaId, professorId));
    }
    
    @PostMapping("/{turmaId}/alunos/{alunoId}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> adicionarAluno(
            @PathVariable Long turmaId,
            @PathVariable Long alunoId) {
        return ResponseEntity.ok(turmaService.adicionarAluno(turmaId, alunoId));
    }
    
    @DeleteMapping("/{turmaId}/alunos/{alunoId}")
    @PreAuthorize("hasAnyRole('DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<TurmaDTO> removerAluno(
            @PathVariable Long turmaId,
            @PathVariable Long alunoId) {
        return ResponseEntity.ok(turmaService.removerAluno(turmaId, alunoId));
    }
}

