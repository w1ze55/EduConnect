package com.educonnect.EduConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscolaDTO {
    private Long id;
    private String nome;
    private String endereco;
    private String telefone;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private DiretorSimplificadoDTO diretor;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiretorSimplificadoDTO {
        private Long id;
        private String nome;
        private String email;
    }
}
