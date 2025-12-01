package com.educonnect.EduConnect.dto;

import com.educonnect.EduConnect.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "CPF deve estar no formato 000.000.000-00")
    private String cpf;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$", message = "Telefone deve estar no formato (00) 00000-0000 ou (00) 0000-0000")
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
