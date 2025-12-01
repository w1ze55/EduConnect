package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecadoResumoDTO {
    private Long id;
    private String titulo;
    private String conteudo;
    private String remetente;
    private Long remetenteId;
    private String categoria;
    private Boolean importante;
    private Boolean lido;
    private LocalDateTime dataEnvio;
}
