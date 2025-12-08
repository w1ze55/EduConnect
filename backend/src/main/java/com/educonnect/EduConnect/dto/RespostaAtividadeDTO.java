package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.StatusAtividade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaAtividadeDTO {
    private Long id;
    private Long atividadeId;
    private Long alunoId;
    private String alunoNome;
    private String alunoEmail;
    private String comentario;
    private List<String> anexos;
    private StatusAtividade status;
    private LocalDateTime dataEnvio;
    private Double nota;
    private String feedback;
    private LocalDateTime dataAvaliacao;
}
