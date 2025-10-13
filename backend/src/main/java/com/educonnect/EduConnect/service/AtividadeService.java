package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.AtividadeCreateDTO;
import com.educonnect.EduConnect.dto.AtividadeDTO;
import com.educonnect.EduConnect.model.Atividade;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.AtividadeRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtividadeService {
    
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    
    @Transactional(readOnly = true)
    public List<AtividadeDTO> listarTodas(Usuario usuarioLogado) {
        List<Atividade> atividades;
        
        // ADMINISTRADOR vê todas as atividades
        if (usuarioLogado.getRole() == Role.ADMINISTRADOR) {
            atividades = atividadeRepository.findAll();
        }
        // DIRETORIA vê atividades da sua escola
        else if (usuarioLogado.getRole() == Role.DIRETORIA) {
            if (usuarioLogado.getEscola() == null) {
                throw new RuntimeException("Diretor não está vinculado a nenhuma escola");
            }
            // Buscar todas as atividades de professores da mesma escola
            atividades = atividadeRepository.findAll().stream()
                .filter(a -> a.getProfessor().getEscola() != null && 
                           a.getProfessor().getEscola().getId().equals(usuarioLogado.getEscola().getId()))
                .collect(Collectors.toList());
        }
        // PROFESSOR vê suas próprias atividades
        else if (usuarioLogado.getRole() == Role.PROFESSOR) {
            atividades = atividadeRepository.findByProfessorId(usuarioLogado.getId());
        }
        // ALUNO vê atividades da sua turma
        else if (usuarioLogado.getRole() == Role.ALUNO) {
            if (usuarioLogado.getTurma() == null || usuarioLogado.getTurma().isEmpty()) {
                throw new RuntimeException("Aluno não possui turma atribuída");
            }
            atividades = atividadeRepository.findByTurmaOrderByDataEntregaDesc(usuarioLogado.getTurma());
        }
        // RESPONSAVEL vê atividades dos alunos vinculados
        else if (usuarioLogado.getRole() == Role.RESPONSAVEL) {
            List<Usuario> alunos = usuarioLogado.getAlunosVinculados();
            if (alunos == null || alunos.isEmpty()) {
                throw new RuntimeException("Responsável não possui alunos vinculados");
            }
            // Buscar atividades de todas as turmas dos alunos vinculados
            List<String> turmas = alunos.stream()
                .map(Usuario::getTurma)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            atividades = atividadeRepository.findAll().stream()
                .filter(a -> turmas.contains(a.getTurma()))
                .collect(Collectors.toList());
        }
        else {
            throw new RuntimeException("Acesso não autorizado");
        }
        
        return atividades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AtividadeDTO buscarPorId(Long id, Usuario usuarioLogado) {
        Atividade atividade = atividadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
        
        // Verificar permissão de visualização
        validarPermissaoVisualizacao(atividade, usuarioLogado);
        
        return convertToDTO(atividade);
    }
    
    @Transactional
    public AtividadeDTO criar(AtividadeCreateDTO dto, Usuario usuarioLogado) {
        // Validar permissão (apenas PROFESSOR, DIRETORIA e ADMINISTRADOR podem criar)
        if (usuarioLogado.getRole() != Role.PROFESSOR && 
            usuarioLogado.getRole() != Role.DIRETORIA && 
            usuarioLogado.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Apenas professores, diretores e administradores podem criar atividades");
        }
        
        // Se for PROFESSOR, deve ter a disciplina nas suas disciplinas
        if (usuarioLogado.getRole() == Role.PROFESSOR) {
            if (usuarioLogado.getDisciplinas() == null || 
                !usuarioLogado.getDisciplinas().contains(dto.getDisciplina())) {
                throw new RuntimeException("Você não pode criar atividades para esta disciplina");
            }
            
            // Verificar se o professor leciona para a turma
            if (usuarioLogado.getTurmas() == null || 
                !usuarioLogado.getTurmas().contains(dto.getTurma())) {
                throw new RuntimeException("Você não leciona para esta turma");
            }
        }
        
        // Se for DIRETORIA, precisa ser da mesma escola
        if (usuarioLogado.getRole() == Role.DIRETORIA && usuarioLogado.getEscola() != null) {
            // Validar se a turma existe na escola (poderia adicionar validação mais específica)
            // Por enquanto, permitir que o diretor crie atividades para qualquer turma da escola
        }
        
        Atividade atividade = new Atividade();
        atividade.setTitulo(dto.getTitulo());
        atividade.setDescricao(dto.getDescricao());
        atividade.setDisciplina(dto.getDisciplina());
        atividade.setTurma(dto.getTurma());
        atividade.setProfessor(usuarioLogado);
        atividade.setDataEntrega(dto.getDataEntrega());
        atividade.setValor(dto.getValor() != null ? dto.getValor() : 10.0);
        atividade.setPeso(dto.getPeso() != null ? dto.getPeso() : 1);
        atividade.setTentativasPermitidas(dto.getTentativasPermitidas() != null ? dto.getTentativasPermitidas() : 1);
        
        if (dto.getAnexos() != null) {
            atividade.setAnexos(dto.getAnexos());
        }
        
        atividade = atividadeRepository.save(atividade);
        return convertToDTO(atividade);
    }
    
    @Transactional
    public AtividadeDTO atualizar(Long id, AtividadeCreateDTO dto, Usuario usuarioLogado) {
        Atividade atividade = atividadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
        
        // Verificar permissão de edição
        validarPermissaoEdicao(atividade, usuarioLogado);
        
        atividade.setTitulo(dto.getTitulo());
        atividade.setDescricao(dto.getDescricao());
        atividade.setDisciplina(dto.getDisciplina());
        atividade.setTurma(dto.getTurma());
        atividade.setDataEntrega(dto.getDataEntrega());
        atividade.setValor(dto.getValor() != null ? dto.getValor() : 10.0);
        atividade.setPeso(dto.getPeso() != null ? dto.getPeso() : 1);
        atividade.setTentativasPermitidas(dto.getTentativasPermitidas() != null ? dto.getTentativasPermitidas() : 1);
        
        if (dto.getAnexos() != null) {
            atividade.setAnexos(dto.getAnexos());
        }
        
        atividade = atividadeRepository.save(atividade);
        return convertToDTO(atividade);
    }
    
    @Transactional
    public void deletar(Long id, Usuario usuarioLogado) {
        Atividade atividade = atividadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
        
        // Verificar permissão de exclusão
        validarPermissaoEdicao(atividade, usuarioLogado);
        
        atividadeRepository.delete(atividade);
    }
    
    private void validarPermissaoVisualizacao(Atividade atividade, Usuario usuario) {
        // ADMINISTRADOR pode ver tudo
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return;
        }
        
        // DIRETORIA pode ver atividades da sua escola
        if (usuario.getRole() == Role.DIRETORIA) {
            if (usuario.getEscola() == null || atividade.getProfessor().getEscola() == null ||
                !usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }
        
        // PROFESSOR pode ver suas próprias atividades
        if (usuario.getRole() == Role.PROFESSOR) {
            if (!atividade.getProfessor().getId().equals(usuario.getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }
        
        // ALUNO pode ver atividades da sua turma
        if (usuario.getRole() == Role.ALUNO) {
            if (usuario.getTurma() == null || !usuario.getTurma().equals(atividade.getTurma())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }
        
        // RESPONSAVEL pode ver atividades dos alunos vinculados
        if (usuario.getRole() == Role.RESPONSAVEL) {
            List<Usuario> alunos = usuario.getAlunosVinculados();
            if (alunos == null || alunos.isEmpty()) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            
            boolean temPermissao = alunos.stream()
                .anyMatch(aluno -> aluno.getTurma() != null && aluno.getTurma().equals(atividade.getTurma()));
            
            if (!temPermissao) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }
        
        throw new RuntimeException("Acesso não autorizado");
    }
    
    private void validarPermissaoEdicao(Atividade atividade, Usuario usuario) {
        // ADMINISTRADOR pode editar tudo
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return;
        }
        
        // DIRETORIA pode editar atividades da sua escola
        if (usuario.getRole() == Role.DIRETORIA) {
            if (usuario.getEscola() == null || atividade.getProfessor().getEscola() == null ||
                !usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para editar esta atividade");
            }
            return;
        }
        
        // PROFESSOR pode editar apenas suas próprias atividades
        if (usuario.getRole() == Role.PROFESSOR) {
            if (!atividade.getProfessor().getId().equals(usuario.getId())) {
                throw new RuntimeException("Você não tem permissão para editar esta atividade");
            }
            return;
        }
        
        throw new RuntimeException("Você não tem permissão para editar esta atividade");
    }
    
    private AtividadeDTO convertToDTO(Atividade atividade) {
        AtividadeDTO dto = new AtividadeDTO();
        dto.setId(atividade.getId());
        dto.setTitulo(atividade.getTitulo());
        dto.setDescricao(atividade.getDescricao());
        dto.setDisciplina(atividade.getDisciplina());
        dto.setTurma(atividade.getTurma());
        dto.setProfessorId(atividade.getProfessor().getId());
        dto.setProfessorNome(atividade.getProfessor().getNome());
        dto.setDataCriacao(atividade.getDataCriacao());
        dto.setDataEntrega(atividade.getDataEntrega());
        dto.setValor(atividade.getValor());
        dto.setPeso(atividade.getPeso());
        dto.setTentativasPermitidas(atividade.getTentativasPermitidas());
        dto.setAnexos(atividade.getAnexos());
        
        if (atividade.getRespostas() != null) {
            dto.setTotalRespostas(atividade.getRespostas().size());
            dto.setRespostasCorrigidas((int) atividade.getRespostas().stream()
                .filter(r -> r.getNota() != null)
                .count());
        }
        
        return dto;
    }
}

