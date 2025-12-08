package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.CategoriaRecado;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecadoDTO {
    private Long id;
    private String titulo;
    private String conteudo;
    private CategoriaRecado categoria;
    private Boolean importante;
    private Boolean exigirConfirmacao;
    private List<String> destinatarios;
    private List<Long> destinatariosEspecificos;
    private List<String> turmasDestinatarias;
    private String remetente;
    private Long remetenteId;
    private LocalDateTime dataEnvio;
    private List<String> anexos;
    private Boolean lido;
    private LocalDateTime dataLeitura;
}

