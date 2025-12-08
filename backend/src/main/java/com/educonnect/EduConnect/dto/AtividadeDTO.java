package com.educonnect.EduConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private String disciplina;
    private String turma;
    private Long professorId;
    private String professorNome;
    private Long escolaId;
    private String escolaNome;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataEntrega;
    private Double valor;
    private Integer peso;
    private Integer tentativasPermitidas;
    private List<String> anexos;
    private Integer totalRespostas;
    private Integer respostasCorrigidas;
}

