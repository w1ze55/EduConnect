package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.AssinaturaDocumentoDTO;
import com.educonnect.EduConnect.dto.DocumentoCreateDTO;
import com.educonnect.EduConnect.dto.DocumentoDTO;
import com.educonnect.EduConnect.model.AssinaturaDocumento;
import com.educonnect.EduConnect.model.Documento;
import com.educonnect.EduConnect.model.Escola;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.AssinaturaDocumentoRepository;
import com.educonnect.EduConnect.repository.DocumentoRepository;
import com.educonnect.EduConnect.repository.EscolaRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    
    private final DocumentoRepository documentoRepository;
    private final AssinaturaDocumentoRepository assinaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Transactional
    public DocumentoDTO criarDocumento(DocumentoCreateDTO dto, MultipartFile arquivo, 
                                       List<MultipartFile> anexos, Usuario criador) {
        // Criar diretório se não existir
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar diretório de upload", e);
            }
        }
        
        Documento documento = new Documento();
        documento.setNome(dto.getNome());
        documento.setDescricao(dto.getDescricao());
        documento.setRequerAssinatura(dto.getRequerAssinatura() != null ? dto.getRequerAssinatura() : false);
        documento.setCriador(criador);
        
        // Associar escola se fornecida ou usar a escola do criador
        if (dto.getEscolaId() != null) {
            Escola escola = escolaRepository.findById(dto.getEscolaId())
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            documento.setEscola(escola);
        } else if (criador.getEscola() != null) {
            documento.setEscola(criador.getEscola());
        }
        
        // Salvar arquivo principal
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                String nomeOriginal = arquivo.getOriginalFilename();
                String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
                String nomeArquivo = UUID.randomUUID().toString() + extensao;
                Path caminhoArquivo = uploadPath.resolve(nomeArquivo);
                
                Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
                
                documento.setNomeArquivo(nomeOriginal);
                documento.setCaminhoArquivo(caminhoArquivo.toString());
                documento.setTipoArquivo(arquivo.getContentType());
                documento.setTamanhoArquivo(arquivo.getSize());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar arquivo", e);
            }
        } else {
            throw new RuntimeException("Arquivo é obrigatório");
        }
        
        // Salvar anexos
        List<String> caminhosAnexos = new ArrayList<>();
        List<String> nomesAnexos = new ArrayList<>();
        
        if (anexos != null && !anexos.isEmpty()) {
            for (MultipartFile anexo : anexos) {
                if (!anexo.isEmpty()) {
                    try {
                        String nomeOriginal = anexo.getOriginalFilename();
                        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
                        String nomeAnexo = UUID.randomUUID().toString() + extensao;
                        Path caminhoAnexo = uploadPath.resolve("anexos").resolve(nomeAnexo);
                        
                        // Criar diretório de anexos se não existir
                        Files.createDirectories(caminhoAnexo.getParent());
                        
                        Files.copy(anexo.getInputStream(), caminhoAnexo, StandardCopyOption.REPLACE_EXISTING);
                        
                        caminhosAnexos.add(caminhoAnexo.toString());
                        nomesAnexos.add(nomeOriginal);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao salvar anexo: " + anexo.getOriginalFilename(), e);
                    }
                }
            }
        }
        
        documento.setAnexos(caminhosAnexos);
        documento.setNomesAnexos(nomesAnexos);
        
        documento = documentoRepository.save(documento);
        return convertToDTO(documento);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarDocumentos(Usuario usuario) {
        List<Documento> documentos;
        
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            documentos = documentoRepository.findAll();
        } else if (usuario.getEscola() != null) {
            documentos = documentoRepository.findByEscolaId(usuario.getEscola().getId());
        } else {
            documentos = documentoRepository.findByCriadorId(usuario.getId());
        }
        
        return documentos.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DocumentoDTO buscarPorId(Long id, Usuario usuario) {
        Documento documento = documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // Verificar permissão
        if (usuario.getRole() != Role.ADMINISTRADOR) {
            if (documento.getEscola() != null && usuario.getEscola() != null) {
                if (!documento.getEscola().getId().equals(usuario.getEscola().getId())) {
                    throw new RuntimeException("Acesso negado a este documento");
                }
            } else if (documento.getEscola() == null && usuario.getEscola() != null) {
                throw new RuntimeException("Acesso negado a este documento");
            }
        }
        
        return convertToDTO(documento);
    }
    
    @Transactional
    public void assinarDocumento(Long documentoId, String assinaturaBase64, String observacoes, Usuario assinante) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        if (!documento.getRequerAssinatura()) {
            throw new RuntimeException("Este documento não requer assinatura");
        }
        
        // Verificar se já assinou
        assinaturaRepository.findByDocumentoIdAndUsuarioId(documentoId, assinante.getId())
            .ifPresent(a -> {
                throw new RuntimeException("Você já assinou este documento");
            });
        
        AssinaturaDocumento assinatura = new AssinaturaDocumento();
        assinatura.setDocumento(documento);
        assinatura.setAssinante(assinante);
        assinatura.setAssinaturaBase64(assinaturaBase64);
        assinatura.setObservacoes(observacoes);
        
        assinaturaRepository.save(assinatura);
        
        // Verificar se todas as assinaturas necessárias foram feitas
        // Por enquanto, consideramos que uma assinatura é suficiente
        documento.setAssinado(true);
        documentoRepository.save(documento);
    }
    
    @Transactional
    public byte[] downloadDocumento(Long id, Usuario usuario) {
        Documento documento = documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // Verificar permissão
        if (usuario.getRole() != Role.ADMINISTRADOR) {
            if (documento.getEscola() != null && usuario.getEscola() != null) {
                if (!documento.getEscola().getId().equals(usuario.getEscola().getId())) {
                    throw new RuntimeException("Acesso negado a este documento");
                }
            } else if (documento.getEscola() == null && usuario.getEscola() != null) {
                throw new RuntimeException("Acesso negado a este documento");
            }
        }
        
        try {
            Path caminhoArquivo = Paths.get(documento.getCaminhoArquivo());
            return Files.readAllBytes(caminhoArquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo", e);
        }
    }
    
    @Transactional
    public byte[] downloadAnexo(Long documentoId, Integer indiceAnexo, Usuario usuario) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // Verificar permissão
        if (usuario.getRole() != Role.ADMINISTRADOR) {
            if (documento.getEscola() != null && usuario.getEscola() != null) {
                if (!documento.getEscola().getId().equals(usuario.getEscola().getId())) {
                    throw new RuntimeException("Acesso negado a este documento");
                }
            } else if (documento.getEscola() == null && usuario.getEscola() != null) {
                throw new RuntimeException("Acesso negado a este documento");
            }
        }
        
        if (documento.getAnexos() == null || indiceAnexo >= documento.getAnexos().size()) {
            throw new RuntimeException("Anexo não encontrado");
        }
        
        try {
            Path caminhoAnexo = Paths.get(documento.getAnexos().get(indiceAnexo));
            return Files.readAllBytes(caminhoAnexo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler anexo", e);
        }
    }
    
    @Transactional
    public void deletarDocumento(Long id, Usuario usuario) {
        Documento documento = documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // Verificar permissão (apenas criador ou admin pode deletar)
        if (usuario.getRole() != Role.ADMINISTRADOR && 
            !documento.getCriador().getId().equals(usuario.getId())) {
            throw new RuntimeException("Você não tem permissão para deletar este documento");
        }
        
        // Deletar arquivos físicos
        try {
            Path caminhoArquivo = Paths.get(documento.getCaminhoArquivo());
            if (Files.exists(caminhoArquivo)) {
                Files.delete(caminhoArquivo);
            }
            
            // Deletar anexos
            if (documento.getAnexos() != null) {
                for (String caminhoAnexo : documento.getAnexos()) {
                    Path pathAnexo = Paths.get(caminhoAnexo);
                    if (Files.exists(pathAnexo)) {
                        Files.delete(pathAnexo);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivos físicos: " + e.getMessage());
        }
        
        documentoRepository.delete(documento);
    }
    
    private DocumentoDTO convertToDTO(Documento documento) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setId(documento.getId());
        dto.setNome(documento.getNome());
        dto.setDescricao(documento.getDescricao());
        dto.setNomeArquivo(documento.getNomeArquivo());
        dto.setTipoArquivo(documento.getTipoArquivo());
        dto.setTamanhoArquivo(documento.getTamanhoArquivo());
        dto.setRequerAssinatura(documento.getRequerAssinatura());
        dto.setAssinado(documento.getAssinado());
        dto.setDataCriacao(documento.getDataCriacao());
        dto.setDataAtualizacao(documento.getDataAtualizacao());
        
        if (documento.getCriador() != null) {
            dto.setCriadorId(documento.getCriador().getId());
            dto.setCriadorNome(documento.getCriador().getNome());
        }
        
        if (documento.getEscola() != null) {
            dto.setEscolaId(documento.getEscola().getId());
            dto.setEscolaNome(documento.getEscola().getNome());
        }
        
        if (documento.getAssinaturas() != null) {
            dto.setAssinaturas(documento.getAssinaturas().stream()
                .map(this::convertAssinaturaToDTO)
                .collect(Collectors.toList()));
        }
        
        dto.setAnexos(documento.getAnexos() != null ? documento.getAnexos() : new ArrayList<>());
        dto.setNomesAnexos(documento.getNomesAnexos() != null ? documento.getNomesAnexos() : new ArrayList<>());
        
        return dto;
    }
    
    private AssinaturaDocumentoDTO convertAssinaturaToDTO(AssinaturaDocumento assinatura) {
        AssinaturaDocumentoDTO dto = new AssinaturaDocumentoDTO();
        dto.setId(assinatura.getId());
        dto.setDocumentoId(assinatura.getDocumento().getId());
        dto.setUsuarioId(assinatura.getAssinante().getId());
        dto.setUsuarioNome(assinatura.getAssinante().getNome());
        dto.setAssinaturaBase64(assinatura.getAssinaturaBase64());
        dto.setDataAssinatura(assinatura.getDataAssinatura());
        dto.setObservacoes(assinatura.getObservacoes());
        return dto;
    }
}

