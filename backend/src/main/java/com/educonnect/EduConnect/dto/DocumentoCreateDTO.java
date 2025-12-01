package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoCreateDTO {
    @NotBlank(message = "Nome do documento é obrigatório")
    private String nome;
    
    private String descricao;
    
    private Boolean requerAssinatura = false;
    
    private Long escolaId;
}

