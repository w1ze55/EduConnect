package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.util.List;

@Data
public class TurmaUpdateDTO {
    private String nome;
    private String anoLetivo;
    private String serie;
    private String turno;
    private String descricao;
    private Boolean ativa;
    private Long escolaId;
    private List<Long> professoresIds;
    private List<Long> alunosIds;
}

