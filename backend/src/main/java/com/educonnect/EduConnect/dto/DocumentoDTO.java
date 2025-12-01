package com.educonnect.EduConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String nomeArquivo;
    private String tipoArquivo;
    private Long tamanhoArquivo;
    private Long criadorId;
    private String criadorNome;
    private Long escolaId;
    private String escolaNome;
    private Boolean requerAssinatura;
    private Boolean assinado;
    private List<AssinaturaDocumentoDTO> assinaturas = new ArrayList<>();
    private List<String> anexos = new ArrayList<>();
    private List<String> nomesAnexos = new ArrayList<>();
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}

