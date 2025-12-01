package com.educonnect.EduConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssinaturaDocumentoDTO {
    private Long id;
    private Long documentoId;
    private Long usuarioId;
    private String usuarioNome;
    private String assinaturaBase64;
    private LocalDateTime dataAssinatura;
    private String observacoes;
}

