package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeCreateDTO {
    
    @NotBlank(message = "Título é obrigatório")
    private String titulo;
    
    private String descricao;
    
    @NotBlank(message = "Disciplina é obrigatória")
    private String disciplina;
    
    @NotBlank(message = "Turma é obrigatória")
    private String turma;
    
    @NotNull(message = "Data de entrega é obrigatória")
    private LocalDateTime dataEntrega;
    
    private Double valor = 10.0;
    private Integer peso = 1;
    private Integer tentativasPermitidas = 1;
    private List<String> anexos;
}

