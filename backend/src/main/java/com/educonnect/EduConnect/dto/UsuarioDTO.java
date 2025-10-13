package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.Role;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private Role role;
    private Boolean ativo;
    private String turma;
    private String matricula;
    private List<String> disciplinas;
    private List<String> turmas;
    private Long responsavelId;
    private String responsavelNome;
    private Long escolaId;
    private String escolaNome;
}

