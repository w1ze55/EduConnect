package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.UsuarioCreateDTO;
import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Escola;
import com.educonnect.EduConnect.model.Turma;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.EscolaRepository;
import com.educonnect.EduConnect.repository.TurmaRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return convertToDTO(usuario);
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarPorRole(Role role) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarDiretoresDisponiveis() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DIRETORIA)
                .filter(u -> u.getAtivo())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarResponsaveisDisponiveis(Long escolaId) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.RESPONSAVEL)
                .filter(u -> u.getAtivo())
                .filter(u -> escolaId == null || (u.getEscola() != null && u.getEscola().getId().equals(escolaId)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UsuarioDTO criar(UsuarioCreateDTO dto) {
        // Validações
        if (dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
            throw new RuntimeException("Senha é obrigatória");
        }
        
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este e-mail");
        }
        
        if (usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este CPF");
        }
        
        // Validar escola (obrigatória exceto para ADMINISTRADOR)
        if (dto.getRole() != Role.ADMINISTRADOR && dto.getEscolaId() == null) {
            throw new RuntimeException("Escola é obrigatória para este tipo de usuário");
        }
        
        // Validar responsável para ALUNO
        if (dto.getRole() == Role.ALUNO && dto.getResponsavelId() == null) {
            throw new RuntimeException("Responsável é obrigatório para alunos");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getSenha()));
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setRole(dto.getRole());
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        // Associar escola
        if (dto.getEscolaId() != null) {
            Escola escola = escolaRepository.findById(dto.getEscolaId())
                    .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            usuario.setEscola(escola);
        }
        
        // Associar responsável (para ALUNO)
        if (dto.getResponsavelId() != null) {
            Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            
            if (responsavel.getRole() != Role.RESPONSAVEL) {
                throw new RuntimeException("O usuário selecionado não é um responsável");
            }
            
            usuario.setResponsavel(responsavel);
        }
        
        // Campos específicos para ALUNO
        if (dto.getRole() == Role.ALUNO) {
            usuario.setTurma(dto.getTurma());
            usuario.setMatricula(dto.getMatricula());
        }
        
        // Campos específicos para PROFESSOR
        if (dto.getRole() == Role.PROFESSOR) {
            usuario.setDisciplinas(dto.getDisciplinas());
            usuario.setTurmas(dto.getTurmas());
        }
        
        usuario = usuarioRepository.save(usuario);
        
        // Se for ALUNO e tiver turmaId, adicionar o aluno à turma
        if (dto.getRole() == Role.ALUNO && dto.getTurmaId() != null) {
            Turma turma = turmaRepository.findById(dto.getTurmaId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
            
            // Adicionar aluno à lista de alunos da turma
            if (turma.getAlunos() == null) {
                turma.setAlunos(new java.util.ArrayList<>());
            }
            
            if (!turma.getAlunos().contains(usuario)) {
                turma.getAlunos().add(usuario);
                turmaRepository.save(turma);
            }
        }
        
        return convertToDTO(usuario);
    }
    
    @Transactional
    public UsuarioDTO atualizar(Long id, UsuarioCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se o email já está em uso por outro usuário
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este e-mail");
        }
        
        // Verificar se o CPF já está em uso por outro usuário
        if (!usuario.getCpf().equals(dto.getCpf()) && usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("Já existe um usuário cadastrado com este CPF");
        }
        
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setRole(dto.getRole());
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        // Atualizar senha apenas se fornecida
        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getSenha()));
        }
        
        // Atualizar escola
        if (dto.getEscolaId() != null) {
            Escola escola = escolaRepository.findById(dto.getEscolaId())
                    .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            usuario.setEscola(escola);
        } else if (dto.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Escola é obrigatória para este tipo de usuário");
        }
        
        // Atualizar responsável (para ALUNO)
        if (dto.getRole() == Role.ALUNO) {
            if (dto.getResponsavelId() == null) {
                throw new RuntimeException("Responsável é obrigatório para alunos");
            }
            Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            usuario.setResponsavel(responsavel);
            usuario.setTurma(dto.getTurma());
            usuario.setMatricula(dto.getMatricula());
            
            // Se turmaId mudou, atualizar vínculo
            if (dto.getTurmaId() != null) {
                Turma turma = turmaRepository.findById(dto.getTurmaId())
                        .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
                
                // Adicionar aluno à nova turma se ainda não estiver
                if (turma.getAlunos() == null) {
                    turma.setAlunos(new java.util.ArrayList<>());
                }
                
                if (!turma.getAlunos().contains(usuario)) {
                    turma.getAlunos().add(usuario);
                    turmaRepository.save(turma);
                }
            }
        }
        
        // Atualizar campos específicos para PROFESSOR
        if (dto.getRole() == Role.PROFESSOR) {
            usuario.setDisciplinas(dto.getDisciplinas());
            usuario.setTurmas(dto.getTurmas());
        }
        
        usuario = usuarioRepository.save(usuario);
        return convertToDTO(usuario);
    }
    
    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se há alunos vinculados (se for responsável)
        if (usuario.getRole() == Role.RESPONSAVEL && 
            usuario.getAlunosVinculados() != null && 
            !usuario.getAlunosVinculados().isEmpty()) {
            throw new RuntimeException("Não é possível excluir um responsável com alunos vinculados");
        }
        
        // Verificar se é diretor de alguma escola
        if (usuario.getRole() == Role.DIRETORIA) {
            List<Escola> escolas = escolaRepository.findAll();
            boolean isDiretor = escolas.stream()
                    .anyMatch(e -> e.getDiretor() != null && e.getDiretor().getId().equals(id));
            if (isDiretor) {
                throw new RuntimeException("Não é possível excluir um diretor que está atribuído a uma escola");
            }
        }
        
        usuarioRepository.delete(usuario);
    }
    
    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(usuario.getCpf());
        dto.setTelefone(usuario.getTelefone());
        dto.setRole(usuario.getRole());
        dto.setAtivo(usuario.getAtivo());
        dto.setTurma(usuario.getTurma());
        dto.setMatricula(usuario.getMatricula());
        dto.setDisciplinas(usuario.getDisciplinas());
        dto.setTurmas(usuario.getTurmas());
        
        if (usuario.getEscola() != null) {
            dto.setEscolaId(usuario.getEscola().getId());
            dto.setEscolaNome(usuario.getEscola().getNome());
        }
        
        if (usuario.getResponsavel() != null) {
            dto.setResponsavelId(usuario.getResponsavel().getId());
            dto.setResponsavelNome(usuario.getResponsavel().getNome());
        }
        
        return dto;
    }
}
