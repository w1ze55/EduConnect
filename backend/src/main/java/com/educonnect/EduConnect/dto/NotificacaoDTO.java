package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.TipoNotificacao;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacaoDTO {
    private Long id;
    private TipoNotificacao tipo;
    private String titulo;
    private String mensagem;
    private String link;
    private String referenciaTipo;
    private Long referenciaId;
    private Boolean lida;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataLeitura;
}
