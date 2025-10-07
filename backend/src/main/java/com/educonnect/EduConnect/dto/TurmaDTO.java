package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TurmaDTO {
    private Long id;
    private String nome;
    private String anoLetivo;
    private String serie;
    private String turno;
    private String descricao;
    private Boolean ativa;
    private Long escolaId;
    private String escolaNome;
    private List<UsuarioSimplificadoDTO> professores = new ArrayList<>();
    private List<AlunoComResponsavelDTO> alunos = new ArrayList<>();
    private Integer totalProfessores;
    private Integer totalAlunos;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    
    @Data
    public static class UsuarioSimplificadoDTO {
        private Long id;
        private String nome;
        private String email;
        private String telefone;
    }
    
    @Data
    public static class AlunoComResponsavelDTO {
        private Long id;
        private String nome;
        private String email;
        private String matricula;
        private ResponsavelSimplificadoDTO responsavel;
    }
    
    @Data
    public static class ResponsavelSimplificadoDTO {
        private Long id;
        private String nome;
        private String email;
        private String telefone;
    }
}

