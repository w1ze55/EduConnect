package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;
    
    // Senha é obrigatória apenas na criação, não na edição
    private String senha;
    
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;
    
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;
    
    @NotNull(message = "Perfil é obrigatório")
    private Role role;
    
    private Boolean ativo = true;
    
    // Escola (obrigatório exceto para ADMINISTRADOR)
    private Long escolaId;
    
    // Campos específicos para ALUNO
    private String turma;
    private String matricula;
    private String dataNascimento;
    private Long responsavelId; // Obrigatório para ALUNO
    
    // Campos específicos para PROFESSOR
    private String formacaoAcademica;
    private List<String> disciplinas;
    private List<String> turmas;
}
