package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.RespostaAtividadeDTO;
import com.educonnect.EduConnect.model.Atividade;
import com.educonnect.EduConnect.model.RespostaAtividade;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.model.enums.StatusAtividade;
import com.educonnect.EduConnect.repository.AtividadeRepository;
import com.educonnect.EduConnect.repository.RespostaAtividadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RespostaAtividadeService {

    private final RespostaAtividadeRepository respostaAtividadeRepository;
    private final AtividadeRepository atividadeRepository;

    private final Path uploadRoot = Paths.get("uploads").resolve("respostas");

    @Transactional
    public RespostaAtividadeDTO enviarResposta(Long atividadeId,
                                               String comentario,
                                               List<String> links,
                                               MultipartFile[] arquivos,
                                               Usuario aluno) {
        if (aluno.getRole() != Role.ALUNO) {
            throw new RuntimeException("Apenas alunos podem enviar respostas de atividade");
        }

        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));

        if (aluno.getTurma() == null || !aluno.getTurma().equals(atividade.getTurma())) {
            throw new RuntimeException("Você não pode responder esta atividade");
        }

        RespostaAtividade resposta = respostaAtividadeRepository
                .findByAtividadeIdAndAlunoId(atividadeId, aluno.getId())
                .orElse(new RespostaAtividade());

        resposta.setAtividade(atividade);
        resposta.setAluno(aluno);
        resposta.setComentario(comentario);
        resposta.setStatus(StatusAtividade.ENTREGUE);
        resposta.setDataEnvio(LocalDateTime.now());

        List<String> anexos = new ArrayList<>();

        if (links != null) {
            links.stream()
                    .filter(link -> link != null && !link.trim().isEmpty())
                    .forEach(link -> anexos.add(link.trim()));
        }

        if (arquivos != null) {
            for (MultipartFile arquivo : arquivos) {
                if (arquivo != null && !arquivo.isEmpty()) {
                    anexos.add(armazenarArquivo(atividadeId, aluno.getId(), arquivo));
                }
            }
        }

        resposta.setAnexos(anexos);

        resposta = respostaAtividadeRepository.save(resposta);
        return convertToDTO(resposta);
    }

    @Transactional(readOnly = true)
    public List<RespostaAtividadeDTO> listarRespostasPorAtividade(Long atividadeId, Usuario usuario) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));

        validarPermissaoVisualizacao(atividade, usuario);

        return respostaAtividadeRepository.findByAtividadeId(atividadeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RespostaAtividadeDTO buscarRespostaDoAluno(Long atividadeId, Usuario aluno) {
        if (aluno.getRole() != Role.ALUNO) {
            throw new RuntimeException("Apenas alunos podem consultar a própria entrega");
        }

        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));

        if (aluno.getTurma() == null || !aluno.getTurma().equals(atividade.getTurma())) {
            throw new RuntimeException("Você não pode visualizar esta atividade");
        }

        return respostaAtividadeRepository.findByAtividadeIdAndAlunoId(atividadeId, aluno.getId())
                .map(this::convertToDTO)
                .orElse(null);
    }

    private void validarPermissaoVisualizacao(Atividade atividade, Usuario usuario) {
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return;
        }

        if (usuario.getRole() == Role.DIRETORIA) {
            if (usuario.getEscola() == null || atividade.getProfessor().getEscola() == null ||
                !usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar as entregas desta atividade");
            }
            return;
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            if (!atividade.getProfessor().getId().equals(usuario.getId())) {
                throw new RuntimeException("Você não tem permissão para visualizar as entregas desta atividade");
            }
            return;
        }

        throw new RuntimeException("Acesso não autorizado para visualizar entregas");
    }

    private String armazenarArquivo(Long atividadeId, Long alunoId, MultipartFile arquivo) {
        String nomeOriginalSeguro = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "arquivo";
        String originalName = StringUtils.cleanPath(nomeOriginalSeguro);
        String fileName = UUID.randomUUID() + "-" + originalName;

        Path destino = uploadRoot
                .resolve("atividade-" + atividadeId)
                .resolve("aluno-" + alunoId)
                .resolve(fileName)
                .normalize();

        try {
            Files.createDirectories(destino.getParent());
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo da resposta", e);
        }

        return "/uploads/respostas/atividade-" + atividadeId + "/aluno-" + alunoId + "/" + fileName;
    }

    private RespostaAtividadeDTO convertToDTO(RespostaAtividade resposta) {
        RespostaAtividadeDTO dto = new RespostaAtividadeDTO();
        dto.setId(resposta.getId());
        dto.setAtividadeId(resposta.getAtividade() != null ? resposta.getAtividade().getId() : null);
        dto.setAlunoId(resposta.getAluno() != null ? resposta.getAluno().getId() : null);
        dto.setAlunoNome(resposta.getAluno() != null ? resposta.getAluno().getNome() : null);
        dto.setAlunoEmail(resposta.getAluno() != null ? resposta.getAluno().getEmail() : null);
        dto.setComentario(resposta.getComentario());
        dto.setAnexos(resposta.getAnexos());
        dto.setStatus(resposta.getStatus());
        dto.setDataEnvio(resposta.getDataEnvio());
        dto.setNota(resposta.getNota());
        dto.setFeedback(resposta.getFeedback());
        dto.setDataAvaliacao(resposta.getDataAvaliacao());
        return dto;
    }
}
