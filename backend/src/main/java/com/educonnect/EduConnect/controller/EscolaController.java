package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.AtribuirDiretorDTO;
import com.educonnect.EduConnect.dto.EscolaCreateDTO;
import com.educonnect.EduConnect.dto.EscolaDTO;
import com.educonnect.EduConnect.service.EscolaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escolas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EscolaController {
    
    private final EscolaService escolaService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DIRETORIA')")
    public ResponseEntity<List<EscolaDTO>> listarTodas() {
        return ResponseEntity.ok(escolaService.listarTodas());
    }
    
    @GetMapping("/ativas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DIRETORIA')")
    public ResponseEntity<List<EscolaDTO>> listarAtivas() {
        return ResponseEntity.ok(escolaService.listarAtivas());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DIRETORIA')")
    public ResponseEntity<EscolaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(escolaService.buscarPorId(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EscolaDTO> criar(@Valid @RequestBody EscolaCreateDTO dto) {
        EscolaDTO escola = escolaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(escola);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EscolaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EscolaCreateDTO dto) {
        return ResponseEntity.ok(escolaService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        escolaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/atribuir-diretor")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EscolaDTO> atribuirDiretor(@Valid @RequestBody AtribuirDiretorDTO dto) {
        return ResponseEntity.ok(escolaService.atribuirDiretor(dto));
    }
    
    @DeleteMapping("/{escolaId}/remover-diretor")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EscolaDTO> removerDiretor(@PathVariable Long escolaId) {
        return ResponseEntity.ok(escolaService.removerDiretor(escolaId));
    }
}
