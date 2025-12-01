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
        
        // Validar força da senha
        validarForcaSenha(dto.getSenha());
        
        // Validar CPF
        validarCPF(dto.getCpf());
        
        // Validar telefone
        validarTelefone(dto.getTelefone());
        
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
        
        // Sincronizar aluno com a turma
        if (dto.getRole() == Role.ALUNO && dto.getTurma() != null && !dto.getTurma().isEmpty()) {
            sincronizarAlunoComTurma(usuario, dto.getTurma());
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
        
        // Validar CPF
        validarCPF(dto.getCpf());
        
        // Validar telefone
        validarTelefone(dto.getTelefone());
        
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setRole(dto.getRole());
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        // Atualizar senha apenas se fornecida
        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            validarForcaSenha(dto.getSenha());
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
        }
        
        // Atualizar campos específicos para PROFESSOR
        if (dto.getRole() == Role.PROFESSOR) {
            usuario.setDisciplinas(dto.getDisciplinas());
            usuario.setTurmas(dto.getTurmas());
        }
        
        usuario = usuarioRepository.save(usuario);
        
        // Sincronizar aluno com a turma
        if (dto.getRole() == Role.ALUNO && dto.getTurma() != null && !dto.getTurma().isEmpty()) {
            sincronizarAlunoComTurma(usuario, dto.getTurma());
        }
        
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
        
        // Se for professor, remover de todas as turmas antes de excluir
        if (usuario.getRole() == Role.PROFESSOR) {
            final Long professorId = usuario.getId();
            List<Turma> turmas = turmaRepository.findByProfessorId(professorId);
            for (Turma turma : turmas) {
                turma.getProfessores().removeIf(p -> p.getId().equals(professorId));
                turmaRepository.save(turma);
            }
        }
        
        usuarioRepository.delete(usuario);
    }
    
    /**
     * Valida a força da senha conforme os critérios de segurança
     */
    public static void validarForcaSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new RuntimeException("Senha é obrigatória");
        }
        
        if (senha.length() < 8) {
            throw new RuntimeException("A senha deve ter no mínimo 8 caracteres");
        }
        
        if (!senha.matches(".*[A-Z].*")) {
            throw new RuntimeException("A senha deve conter pelo menos uma letra maiúscula");
        }
        
        if (!senha.matches(".*[a-z].*")) {
            throw new RuntimeException("A senha deve conter pelo menos uma letra minúscula");
        }
        
        if (!senha.matches(".*[0-9].*")) {
            throw new RuntimeException("A senha deve conter pelo menos um número");
        }
        
        if (!senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new RuntimeException("A senha deve conter pelo menos um caractere especial (!@#$%^&*)");
        }
    }
    
    /**
     * Valida o formato do CPF
     */
    public static void validarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new RuntimeException("CPF é obrigatório");
        }
        
        // Remove caracteres não numéricos
        String cpfLimpo = cpf.replaceAll("\\D", "");
        
        // Verifica se tem 11 dígitos
        if (cpfLimpo.length() != 11) {
            throw new RuntimeException("CPF deve conter 11 dígitos");
        }
        
        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            throw new RuntimeException("CPF inválido");
        }
        
        // Validação do dígito verificador
        int soma = 0;
        int resto;
        
        for (int i = 1; i <= 9; i++) {
            soma += Integer.parseInt(cpfLimpo.substring(i - 1, i)) * (11 - i);
        }
        
        resto = (soma * 10) % 11;
        if (resto == 10 || resto == 11) resto = 0;
        if (resto != Integer.parseInt(cpfLimpo.substring(9, 10))) {
            throw new RuntimeException("CPF inválido");
        }
        
        soma = 0;
        for (int i = 1; i <= 10; i++) {
            soma += Integer.parseInt(cpfLimpo.substring(i - 1, i)) * (12 - i);
        }
        
        resto = (soma * 10) % 11;
        if (resto == 10 || resto == 11) resto = 0;
        if (resto != Integer.parseInt(cpfLimpo.substring(10, 11))) {
            throw new RuntimeException("CPF inválido");
        }
        
        // Verifica formato com máscara
        if (!cpf.matches("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")) {
            throw new RuntimeException("CPF deve estar no formato 000.000.000-00");
        }
    }
    
    /**
     * Valida o formato do telefone
     */
    public static void validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new RuntimeException("Telefone é obrigatório");
        }
        
        // Remove caracteres não numéricos
        String telefoneLimpo = telefone.replaceAll("\\D", "");
        
        // Telefone deve ter 10 ou 11 dígitos (fixo ou celular)
        if (telefoneLimpo.length() != 10 && telefoneLimpo.length() != 11) {
            throw new RuntimeException("Telefone deve conter 10 ou 11 dígitos");
        }
        
        // Verifica formato com máscara
        if (!telefone.matches("^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$")) {
            throw new RuntimeException("Telefone deve estar no formato (00) 00000-0000 ou (00) 0000-0000");
        }
    }
    
    /**
     * Sincroniza o aluno com a turma, adicionando-o à lista de alunos da turma
     */
    private void sincronizarAlunoComTurma(Usuario aluno, String nomeTurma) {
        // Buscar a turma pelo nome e pela escola do aluno
        List<Turma> turmas = turmaRepository.findAll().stream()
            .filter(t -> t.getNome().equals(nomeTurma))
            .filter(t -> aluno.getEscola() != null && t.getEscola() != null && 
                        t.getEscola().getId().equals(aluno.getEscola().getId()))
            .collect(java.util.stream.Collectors.toList());
        
        if (turmas.isEmpty()) {
            System.out.println("⚠️ Turma '" + nomeTurma + "' não encontrada para a escola do aluno");
            return;
        }
        
        Turma turma = turmas.get(0);
        
        // Verificar se o aluno já está na turma
        boolean jaEstaVinculado = turma.getAlunos().stream()
            .anyMatch(a -> a.getId().equals(aluno.getId()));
        
        if (!jaEstaVinculado) {
            turma.getAlunos().add(aluno);
            turmaRepository.save(turma);
            System.out.println("✅ Aluno " + aluno.getNome() + " adicionado à turma " + turma.getNome());
        } else {
            System.out.println("ℹ️ Aluno " + aluno.getNome() + " já está vinculado à turma " + turma.getNome());
        }
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
