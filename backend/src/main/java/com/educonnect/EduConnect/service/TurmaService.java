package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.TurmaCreateDTO;
import com.educonnect.EduConnect.dto.TurmaDTO;
import com.educonnect.EduConnect.dto.TurmaUpdateDTO;
import com.educonnect.EduConnect.model.Escola;
import com.educonnect.EduConnect.model.Turma;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.EscolaRepository;
import com.educonnect.EduConnect.repository.TurmaRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurmaService {
    
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    
    @Transactional(readOnly = true)
    public List<TurmaDTO> listarTodas() {
        Usuario usuario = getUsuarioLogado();
        List<Turma> turmas;
        
        // Administrador vê todas
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            turmas = turmaRepository.findAll();
        }
        // Diretoria vê apenas da sua escola
        else if (usuario.getRole() == Role.DIRETORIA) {
            turmas = turmaRepository.findByEscolaId(usuario.getEscola().getId());
        }
        // Professor vê suas turmas
        else if (usuario.getRole() == Role.PROFESSOR) {
            turmas = turmaRepository.findByProfessorId(usuario.getId());
        }
        // Aluno vê suas turmas
        else if (usuario.getRole() == Role.ALUNO) {
            turmas = turmaRepository.findByAlunoId(usuario.getId());
        }
        // Responsável vê turmas de seus filhos
        else if (usuario.getRole() == Role.RESPONSAVEL) {
            turmas = new ArrayList<>();
            if (usuario.getAlunosVinculados() != null) {
                for (Usuario aluno : usuario.getAlunosVinculados()) {
                    turmas.addAll(turmaRepository.findByAlunoId(aluno.getId()));
                }
            }
        }
        else {
            turmas = new ArrayList<>();
        }
        
        return turmas.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TurmaDTO buscarPorId(Long id) {
        Turma turma = turmaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        // Verificar permissão
        verificarPermissaoVisualizacao(turma);
        
        return convertToDTO(turma);
    }
    
    @Transactional
    public TurmaDTO criar(TurmaCreateDTO dto) {
        Usuario usuario = getUsuarioLogado();
        
        // Apenas DIRETORIA e ADMINISTRADOR podem criar turmas
        if (usuario.getRole() != Role.DIRETORIA && usuario.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Apenas diretoria e administradores podem criar turmas");
        }
        
        Turma turma = new Turma();
        turma.setNome(dto.getNome());
        turma.setAnoLetivo(dto.getAnoLetivo());
        turma.setSerie(dto.getSerie());
        turma.setTurno(dto.getTurno());
        turma.setDescricao(dto.getDescricao());
        turma.setAtiva(dto.getAtiva());
        
        // Definir escola
        if (dto.getEscolaId() != null) {
            Escola escola = escolaRepository.findById(dto.getEscolaId())
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            turma.setEscola(escola);
        } else if (usuario.getRole() == Role.DIRETORIA) {
            turma.setEscola(usuario.getEscola());
        }
        
        // Adicionar professores
        if (dto.getProfessoresIds() != null && !dto.getProfessoresIds().isEmpty()) {
            List<Usuario> professores = usuarioRepository.findAllById(dto.getProfessoresIds());
            // Validar que são professores
            for (Usuario prof : professores) {
                if (prof.getRole() != Role.PROFESSOR) {
                    throw new RuntimeException("Usuário " + prof.getNome() + " não é professor");
                }
            }
            turma.setProfessores(professores);
        }
        
        // Adicionar alunos
        if (dto.getAlunosIds() != null && !dto.getAlunosIds().isEmpty()) {
            List<Usuario> alunos = usuarioRepository.findAllById(dto.getAlunosIds());
            // Validar que são alunos
            for (Usuario aluno : alunos) {
                if (aluno.getRole() != Role.ALUNO) {
                    throw new RuntimeException("Usuário " + aluno.getNome() + " não é aluno");
                }
            }
            turma.setAlunos(alunos);
        }
        
        Turma turmaSalva = turmaRepository.save(turma);
        return convertToDTO(turmaSalva);
    }
    
    @Transactional
    public TurmaDTO atualizar(Long id, TurmaUpdateDTO dto) {
        Usuario usuario = getUsuarioLogado();
        
        // Apenas DIRETORIA e ADMINISTRADOR podem atualizar turmas
        if (usuario.getRole() != Role.DIRETORIA && usuario.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Apenas diretoria e administradores podem atualizar turmas");
        }
        
        Turma turma = turmaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        // Diretoria só pode editar turmas da sua escola
        if (usuario.getRole() == Role.DIRETORIA && 
            !turma.getEscola().getId().equals(usuario.getEscola().getId())) {
            throw new RuntimeException("Você não tem permissão para editar esta turma");
        }
        
        // Atualizar campos
        if (dto.getNome() != null) turma.setNome(dto.getNome());
        if (dto.getAnoLetivo() != null) turma.setAnoLetivo(dto.getAnoLetivo());
        if (dto.getSerie() != null) turma.setSerie(dto.getSerie());
        if (dto.getTurno() != null) turma.setTurno(dto.getTurno());
        if (dto.getDescricao() != null) turma.setDescricao(dto.getDescricao());
        if (dto.getAtiva() != null) turma.setAtiva(dto.getAtiva());
        
        // Atualizar escola (apenas admin)
        if (dto.getEscolaId() != null && usuario.getRole() == Role.ADMINISTRADOR) {
            Escola escola = escolaRepository.findById(dto.getEscolaId())
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            turma.setEscola(escola);
        }
        
        // Atualizar professores
        if (dto.getProfessoresIds() != null) {
            List<Usuario> professores = usuarioRepository.findAllById(dto.getProfessoresIds());
            for (Usuario prof : professores) {
                if (prof.getRole() != Role.PROFESSOR) {
                    throw new RuntimeException("Usuário " + prof.getNome() + " não é professor");
                }
            }
            turma.setProfessores(professores);
        }
        
        // Atualizar alunos
        if (dto.getAlunosIds() != null) {
            List<Usuario> alunos = usuarioRepository.findAllById(dto.getAlunosIds());
            for (Usuario aluno : alunos) {
                if (aluno.getRole() != Role.ALUNO) {
                    throw new RuntimeException("Usuário " + aluno.getNome() + " não é aluno");
                }
            }
            turma.setAlunos(alunos);
        }
        
        Turma turmaAtualizada = turmaRepository.save(turma);
        return convertToDTO(turmaAtualizada);
    }
    
    @Transactional
    public void deletar(Long id) {
        Usuario usuario = getUsuarioLogado();
        
        // Apenas DIRETORIA e ADMINISTRADOR podem deletar turmas
        if (usuario.getRole() != Role.DIRETORIA && usuario.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Apenas diretoria e administradores podem deletar turmas");
        }
        
        Turma turma = turmaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        // Diretoria só pode deletar turmas da sua escola
        if (usuario.getRole() == Role.DIRETORIA && 
            !turma.getEscola().getId().equals(usuario.getEscola().getId())) {
            throw new RuntimeException("Você não tem permissão para deletar esta turma");
        }
        
        turmaRepository.deleteById(id);
    }
    
    @Transactional
    public TurmaDTO adicionarProfessor(Long turmaId, Long professorId) {
        Turma turma = turmaRepository.findById(turmaId)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        Usuario professor = usuarioRepository.findById(professorId)
            .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
        
        if (professor.getRole() != Role.PROFESSOR) {
            throw new RuntimeException("Usuário não é professor");
        }
        
        if (!turma.getProfessores().contains(professor)) {
            turma.getProfessores().add(professor);
            turmaRepository.save(turma);
        }
        
        return convertToDTO(turma);
    }
    
    @Transactional
    public TurmaDTO removerProfessor(Long turmaId, Long professorId) {
        Turma turma = turmaRepository.findById(turmaId)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        turma.getProfessores().removeIf(p -> p.getId().equals(professorId));
        turmaRepository.save(turma);
        
        return convertToDTO(turma);
    }
    
    @Transactional
    public TurmaDTO adicionarAluno(Long turmaId, Long alunoId) {
        Turma turma = turmaRepository.findById(turmaId)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        Usuario aluno = usuarioRepository.findById(alunoId)
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        if (aluno.getRole() != Role.ALUNO) {
            throw new RuntimeException("Usuário não é aluno");
        }
        
        if (!turma.getAlunos().contains(aluno)) {
            turma.getAlunos().add(aluno);
            turmaRepository.save(turma);
        }
        
        return convertToDTO(turma);
    }
    
    @Transactional
    public TurmaDTO removerAluno(Long turmaId, Long alunoId) {
        Turma turma = turmaRepository.findById(turmaId)
            .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        turma.getAlunos().removeIf(a -> a.getId().equals(alunoId));
        turmaRepository.save(turma);
        
        return convertToDTO(turma);
    }
    
    private TurmaDTO convertToDTO(Turma turma) {
        TurmaDTO dto = new TurmaDTO();
        dto.setId(turma.getId());
        dto.setNome(turma.getNome());
        dto.setAnoLetivo(turma.getAnoLetivo());
        dto.setSerie(turma.getSerie());
        dto.setTurno(turma.getTurno());
        dto.setDescricao(turma.getDescricao());
        dto.setAtiva(turma.getAtiva());
        dto.setDataCriacao(turma.getDataCriacao());
        dto.setDataAtualizacao(turma.getDataAtualizacao());
        
        if (turma.getEscola() != null) {
            dto.setEscolaId(turma.getEscola().getId());
            dto.setEscolaNome(turma.getEscola().getNome());
        }
        
        // Converter professores
        if (turma.getProfessores() != null) {
            dto.setProfessores(turma.getProfessores().stream()
                .map(this::convertToUsuarioSimplificado)
                .collect(Collectors.toList()));
            dto.setTotalProfessores(turma.getProfessores().size());
        }
        
        // Converter alunos com responsáveis
        if (turma.getAlunos() != null) {
            dto.setAlunos(turma.getAlunos().stream()
                .map(this::convertToAlunoComResponsavel)
                .collect(Collectors.toList()));
            dto.setTotalAlunos(turma.getAlunos().size());
        }
        
        return dto;
    }
    
    private TurmaDTO.UsuarioSimplificadoDTO convertToUsuarioSimplificado(Usuario usuario) {
        TurmaDTO.UsuarioSimplificadoDTO dto = new TurmaDTO.UsuarioSimplificadoDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setTelefone(usuario.getTelefone());
        return dto;
    }
    
    private TurmaDTO.AlunoComResponsavelDTO convertToAlunoComResponsavel(Usuario aluno) {
        TurmaDTO.AlunoComResponsavelDTO dto = new TurmaDTO.AlunoComResponsavelDTO();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setEmail(aluno.getEmail());
        dto.setMatricula(aluno.getMatricula());
        
        if (aluno.getResponsavel() != null) {
            TurmaDTO.ResponsavelSimplificadoDTO respDto = new TurmaDTO.ResponsavelSimplificadoDTO();
            respDto.setId(aluno.getResponsavel().getId());
            respDto.setNome(aluno.getResponsavel().getNome());
            respDto.setEmail(aluno.getResponsavel().getEmail());
            respDto.setTelefone(aluno.getResponsavel().getTelefone());
            dto.setResponsavel(respDto);
        }
        
        return dto;
    }
    
    private void verificarPermissaoVisualizacao(Turma turma) {
        Usuario usuario = getUsuarioLogado();
        
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return; // Admin pode ver tudo
        }
        
        if (usuario.getRole() == Role.DIRETORIA) {
            if (!turma.getEscola().getId().equals(usuario.getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta turma");
            }
            return;
        }
        
        if (usuario.getRole() == Role.PROFESSOR) {
            boolean isProfessorDaTurma = turma.getProfessores().stream()
                .anyMatch(p -> p.getId().equals(usuario.getId()));
            if (!isProfessorDaTurma) {
                throw new RuntimeException("Você não tem permissão para visualizar esta turma");
            }
            return;
        }
        
        if (usuario.getRole() == Role.ALUNO) {
            boolean isAlunoDaTurma = turma.getAlunos().stream()
                .anyMatch(a -> a.getId().equals(usuario.getId()));
            if (!isAlunoDaTurma) {
                throw new RuntimeException("Você não tem permissão para visualizar esta turma");
            }
            return;
        }
        
        if (usuario.getRole() == Role.RESPONSAVEL) {
            boolean temFilhoNaTurma = false;
            if (usuario.getAlunosVinculados() != null) {
                for (Usuario aluno : usuario.getAlunosVinculados()) {
                    if (turma.getAlunos().stream().anyMatch(a -> a.getId().equals(aluno.getId()))) {
                        temFilhoNaTurma = true;
                        break;
                    }
                }
            }
            if (!temFilhoNaTurma) {
                throw new RuntimeException("Você não tem permissão para visualizar esta turma");
            }
        }
    }
    
    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}

