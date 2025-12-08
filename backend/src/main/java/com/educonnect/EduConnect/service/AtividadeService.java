package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.AtividadeCreateDTO;
import com.educonnect.EduConnect.dto.AtividadeDTO;
import com.educonnect.EduConnect.model.Atividade;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.AtividadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;

    @Transactional(readOnly = true)
    public List<AtividadeDTO> listarTodas(Usuario usuarioLogado, Long escolaFiltro, Long professorFiltro) {
        List<Atividade> atividades;

        if (usuarioLogado.getRole() == Role.ADMINISTRADOR) {
            atividades = atividadeRepository.findAll();
        }
        else if (usuarioLogado.getRole() == Role.DIRETORIA) {
            if (usuarioLogado.getEscola() == null) {
                throw new RuntimeException("Diretor não está vinculado a nenhuma escola");
            }
            atividades = atividadeRepository.findAll().stream()
                .filter(a -> a.getProfessor().getEscola() != null &&
                        a.getProfessor().getEscola().getId().equals(usuarioLogado.getEscola().getId()))
                .collect(Collectors.toList());
        }
        else if (usuarioLogado.getRole() == Role.PROFESSOR) {
            atividades = atividadeRepository.findByProfessorId(usuarioLogado.getId());
        }
        else if (usuarioLogado.getRole() == Role.ALUNO) {
            if (usuarioLogado.getTurma() == null || usuarioLogado.getTurma().isEmpty()) {
                throw new RuntimeException("Aluno não possui turma atribuída");
            }
            atividades = atividadeRepository.findByTurmaOrderByDataEntregaDesc(usuarioLogado.getTurma());
        }
        else if (usuarioLogado.getRole() == Role.RESPONSAVEL) {
            List<Usuario> alunos = usuarioLogado.getAlunosVinculados();
            if (alunos == null || alunos.isEmpty()) {
                throw new RuntimeException("Responsável não possui alunos vinculados");
            }
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

        if (usuarioLogado.getRole() == Role.ADMINISTRADOR || usuarioLogado.getRole() == Role.DIRETORIA) {
            if (escolaFiltro != null) {
                atividades = atividades.stream()
                    .filter(a -> a.getProfessor() != null && a.getProfessor().getEscola() != null
                        && escolaFiltro.equals(a.getProfessor().getEscola().getId()))
                    .collect(Collectors.toList());

                if (usuarioLogado.getRole() == Role.DIRETORIA &&
                    usuarioLogado.getEscola() != null &&
                    !usuarioLogado.getEscola().getId().equals(escolaFiltro)) {
                    throw new RuntimeException("Diretor só pode filtrar pela sua própria escola");
                }
            } else if (usuarioLogado.getRole() == Role.DIRETORIA && usuarioLogado.getEscola() != null) {
                Long escolaDiretor = usuarioLogado.getEscola().getId();
                atividades = atividades.stream()
                    .filter(a -> a.getProfessor() != null && a.getProfessor().getEscola() != null
                        && escolaDiretor.equals(a.getProfessor().getEscola().getId()))
                    .collect(Collectors.toList());
            }

            if (professorFiltro != null) {
                atividades = atividades.stream()
                    .filter(a -> a.getProfessor() != null && professorFiltro.equals(a.getProfessor().getId()))
                    .collect(Collectors.toList());
            }
        }

        return atividades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AtividadeDTO buscarPorId(Long id, Usuario usuarioLogado) {
        Atividade atividade = atividadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));

        validarPermissaoVisualizacao(atividade, usuarioLogado);

        return convertToDTO(atividade);
    }

    @Transactional
    public AtividadeDTO criar(AtividadeCreateDTO dto, Usuario usuarioLogado) {
        if (usuarioLogado.getRole() != Role.PROFESSOR &&
            usuarioLogado.getRole() != Role.DIRETORIA &&
            usuarioLogado.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Apenas professores, diretores e administradores podem criar atividades");
        }

        if (usuarioLogado.getRole() == Role.PROFESSOR) {
            if (usuarioLogado.getDisciplinas() == null ||
                !usuarioLogado.getDisciplinas().contains(dto.getDisciplina())) {
                throw new RuntimeException("Você não pode criar atividades para esta disciplina");
            }

            if (usuarioLogado.getTurmas() == null ||
                !usuarioLogado.getTurmas().contains(dto.getTurma())) {
                throw new RuntimeException("Você não leciona para esta turma");
            }
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

        validarPermissaoEdicao(atividade, usuarioLogado);

        atividadeRepository.delete(atividade);
    }

    private void validarPermissaoVisualizacao(Atividade atividade, Usuario usuario) {
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return;
        }

        if (usuario.getRole() == Role.DIRETORIA) {
            if (usuario.getEscola() == null || atividade.getProfessor().getEscola() == null ||
                !usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            if (!atividade.getProfessor().getId().equals(usuario.getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }

        if (usuario.getRole() == Role.ALUNO) {
            if (usuario.getTurma() == null || !usuario.getTurma().equals(atividade.getTurma())) {
                throw new RuntimeException("Você não tem permissão para visualizar esta atividade");
            }
            return;
        }

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
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return;
        }

        if (usuario.getRole() == Role.DIRETORIA) {
            if (usuario.getEscola() == null || atividade.getProfessor().getEscola() == null ||
                !usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para editar esta atividade");
            }
            return;
        }

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
        if (atividade.getProfessor().getEscola() != null) {
            dto.setEscolaId(atividade.getProfessor().getEscola().getId());
            dto.setEscolaNome(atividade.getProfessor().getEscola().getNome());
        }
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
