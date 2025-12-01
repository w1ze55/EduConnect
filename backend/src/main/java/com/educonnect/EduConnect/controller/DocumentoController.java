package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.DocumentoCreateDTO;
import com.educonnect.EduConnect.dto.DocumentoDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {
    
    private final DocumentoService documentoService;
    
    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> listarDocumentos(
            @AuthenticationPrincipal Usuario usuario) {
        List<DocumentoDTO> documentos = documentoService.listarDocumentos(usuario);
        return ResponseEntity.ok(documentos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        DocumentoDTO documento = documentoService.buscarPorId(id, usuario);
        return ResponseEntity.ok(documento);
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoDTO> criarDocumento(
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "requerAssinatura", required = false) Boolean requerAssinatura,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            DocumentoCreateDTO dados;
            if (dadosJson != null && !dadosJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                dados = objectMapper.readValue(dadosJson, DocumentoCreateDTO.class);
            } else {
                dados = new DocumentoCreateDTO();
                dados.setNome(nome);
                dados.setDescricao(descricao);
                dados.setRequerAssinatura(requerAssinatura != null ? requerAssinatura : false);
            }
            DocumentoDTO documento = documentoService.criarDocumento(dados, arquivo, anexos, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(documento);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar dados do documento", e);
        }
    }
    
    @PostMapping("/{id}/assinar")
    public ResponseEntity<DocumentoDTO> assinarDocumento(
            @PathVariable Long id,
            @RequestBody AssinaturaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        documentoService.assinarDocumento(id, request.getAssinatura(), request.getObservacoes(), usuario);
        DocumentoDTO documento = documentoService.buscarPorId(id, usuario);
        return ResponseEntity.ok(documento);
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocumento(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        byte[] arquivo = documentoService.downloadDocumento(id, usuario);
        
        DocumentoDTO documento = documentoService.buscarPorId(id, usuario);
        String nomeArquivo = documento.getNomeArquivo();
        String tipoArquivo = documento.getTipoArquivo();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(tipoArquivo));
        headers.setContentDispositionFormData("attachment", nomeArquivo);
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(arquivo);
    }
    
    @GetMapping("/{id}/anexos/{indice}/download")
    public ResponseEntity<byte[]> downloadAnexo(
            @PathVariable Long id,
            @PathVariable Integer indice,
            @AuthenticationPrincipal Usuario usuario) {
        byte[] arquivo = documentoService.downloadAnexo(id, indice, usuario);
        
        DocumentoDTO documento = documentoService.buscarPorId(id, usuario);
        String nomeAnexo = documento.getNomesAnexos().get(indice);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", nomeAnexo);
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(arquivo);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletarDocumento(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        documentoService.deletarDocumento(id, usuario);
        return ResponseEntity.noContent().build();
    }
    
    // Classe interna para receber dados da assinatura
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssinaturaRequest {
        private String assinatura; // Base64 da assinatura
        private String observacoes;
    }
}

