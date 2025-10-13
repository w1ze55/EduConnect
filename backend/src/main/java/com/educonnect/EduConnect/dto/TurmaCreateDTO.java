package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TurmaCreateDTO {
    
    @NotBlank(message = "Nome da turma é obrigatório")
    private String nome;
    
    @NotBlank(message = "Ano letivo é obrigatório")
    private String anoLetivo;
    
    @NotBlank(message = "Série é obrigatória")
    private String serie;
    
    @NotBlank(message = "Turno é obrigatório")
    private String turno;
    
    private String descricao;
    
    private Boolean ativa = true;
    
    private Long escolaId;
    
    private List<Long> professoresIds = new ArrayList<>();
    
    private List<Long> alunosIds = new ArrayList<>();
}

