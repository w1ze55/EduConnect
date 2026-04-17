package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.RecadoDTO;
import com.educonnect.EduConnect.model.LeituraRecado;
import com.educonnect.EduConnect.model.Recado;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.TipoNotificacao;
import com.educonnect.EduConnect.repository.LeituraRecadoRepository;
import com.educonnect.EduConnect.repository.RecadoRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecadoService {

    private final RecadoRepository recadoRepository;
    private final LeituraRecadoRepository leituraRecadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final NotificacaoService notificacaoService;

    public List<RecadoDTO> getRecadosParaUsuario() {
        Usuario usuario = getUsuarioLogado();
        List<Recado> recados = recadoRepository.findAllByOrderByDataEnvioDesc();

        return recados.stream()
            .filter(recado -> podeVerRecado(recado, usuario))
            .map(recado -> convertToDTO(recado, usuario))
            .collect(Collectors.toList());
    }

    private boolean podeVerRecado(Recado recado, Usuario usuario) {
        // Filtro de turmas destinatarias (se configurado)
        if (recado.getTurmasDestinatarias() != null && !recado.getTurmasDestinatarias().isEmpty()) {
            String turmaUsuario = usuario.getTurma();
            boolean pertenceTurma = turmaUsuario != null && recado.getTurmasDestinatarias().contains(turmaUsuario);

            if (usuario.getRole().name().equals("RESPONSAVEL") && usuario.getAlunosVinculados() != null) {
                pertenceTurma = usuario.getAlunosVinculados().stream()
                        .anyMatch(filho -> filho.getTurma() != null && recado.getTurmasDestinatarias().contains(filho.getTurma()));
            }

            boolean bypass = usuario.getRole().name().equals("ADMINISTRADOR") ||
                    usuario.getRole().name().equals("DIRETORIA") ||
                    usuario.getRole().name().equals("PROFESSOR");

            if (!pertenceTurma && !bypass) {
                return false;
            }
        }

        // Destinatarios especificos
        if (recado.getDestinatariosEspecificos() != null && !recado.getDestinatariosEspecificos().isEmpty()) {
            if (recado.getDestinatariosEspecificos().contains(usuario.getId())) {
                return true;
            }

            if (usuario.getRole().name().equals("RESPONSAVEL") && usuario.getAlunosVinculados() != null) {
                boolean filhoDestinatario = usuario.getAlunosVinculados().stream()
                        .anyMatch(filho -> recado.getDestinatariosEspecificos().contains(filho.getId()));
                if (filhoDestinatario) return true;
            }

            if (usuario.getRole().name().equals("ADMINISTRADOR") ||
                usuario.getRole().name().equals("DIRETORIA")) {
                return true;
            }

            if (usuario.getRole().name().equals("PROFESSOR")) {
                return true;
            }

            return false;
        }

        if (recado.getDestinatarios() == null || recado.getDestinatarios().isEmpty()) {
            return true;
        }

        if (recado.getDestinatarios().contains("TODOS")) {
            return true;
        }

        String roleUsuario = usuario.getRole().name();

        if (roleUsuario.equals("ADMINISTRADOR") || roleUsuario.equals("DIRETORIA")) {
            return true;
        }

        if (roleUsuario.equals("PROFESSOR")) {
            return recado.getDestinatarios().contains("PROFESSOR") ||
                   recado.getDestinatarios().contains("ALUNO") ||
                   recado.getDestinatarios().contains("RESPONSAVEL");
        }

        if (roleUsuario.equals("RESPONSAVEL")) {
            return recado.getDestinatarios().contains("RESPONSAVEL") ||
                   recado.getDestinatarios().contains("ALUNO");
        }

        if (roleUsuario.equals("ALUNO")) {
            return recado.getDestinatarios().contains("ALUNO");
        }

        return recado.getDestinatarios().contains(roleUsuario);
    }

    public RecadoDTO getRecadoById(Long id) {
        Usuario usuario = getUsuarioLogado();
        Recado recado = recadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recado não encontrado"));

        if (!podeVerRecado(recado, usuario)) {
            throw new RuntimeException("Você não tem permissão para visualizar este recado");
        }

        return convertToDTO(recado, usuario);
    }

    public RecadoDTO criarRecado(Recado recado) {
        Usuario remetente = getUsuarioLogado();
        recado.setRemetente(remetente);

        Recado savedRecado = recadoRepository.save(recado);
        notificacaoService.notificarUsuarios(
            resolverDestinatariosNotificacao(savedRecado),
            remetente.getId(),
            TipoNotificacao.RECADO,
            "Novo recado",
            savedRecado.getTitulo(),
            "/recados/" + savedRecado.getId(),
            "RECADO",
            savedRecado.getId()
        );
        return convertToDTO(savedRecado, remetente);
    }

    public void confirmarLeitura(Long recadoId) {
        Usuario usuario = getUsuarioLogado();
        if (!(usuario.getRole().name().equals("ALUNO") || usuario.getRole().name().equals("RESPONSAVEL"))) {
            throw new RuntimeException("A confirmação de leitura é permitida apenas para alunos e responsáveis");
        }
        Recado recado = recadoRepository.findById(recadoId)
            .orElseThrow(() -> new RuntimeException("Recado não encontrado"));

        LeituraRecado leitura = leituraRecadoRepository
            .findByRecadoIdAndUsuarioId(recadoId, usuario.getId())
            .orElse(new LeituraRecado());

        leitura.setRecado(recado);
        leitura.setUsuario(usuario);
        leitura.setConfirmada(true);

        leituraRecadoRepository.save(leitura);
    }

    public RecadoDTO atualizarRecado(Long id, Recado recadoAtualizado) {
        Usuario usuario = getUsuarioLogado();
        Recado recado = recadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recado não encontrado"));

        if (!recado.getRemetente().getId().equals(usuario.getId()) &&
            !usuario.getRole().name().equals("ADMINISTRADOR") &&
            !usuario.getRole().name().equals("DIRETORIA")) {
            throw new RuntimeException("Você não tem permissão para editar este recado");
        }

        recado.setTitulo(recadoAtualizado.getTitulo());
        recado.setConteudo(recadoAtualizado.getConteudo());
        recado.setCategoria(recadoAtualizado.getCategoria());
        recado.setImportante(recadoAtualizado.getImportante());
        recado.setExigirConfirmacao(recadoAtualizado.getExigirConfirmacao());

        if (recadoAtualizado.getDestinatarios() != null) {
            recado.setDestinatarios(recadoAtualizado.getDestinatarios());
        }

        if (recadoAtualizado.getDestinatariosEspecificos() != null) {
            recado.setDestinatariosEspecificos(recadoAtualizado.getDestinatariosEspecificos());
        }

        if (recadoAtualizado.getTurmasDestinatarias() != null) {
            recado.setTurmasDestinatarias(recadoAtualizado.getTurmasDestinatarias());
        }

        if (recadoAtualizado.getAnexos() != null) {
            recado.setAnexos(recadoAtualizado.getAnexos());
        }

        Recado savedRecado = recadoRepository.save(recado);
        return convertToDTO(savedRecado, usuario);
    }

    public void deletarRecado(Long id) {
        Usuario usuario = getUsuarioLogado();
        Recado recado = recadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recado não encontrado"));

        if (!recado.getRemetente().getId().equals(usuario.getId()) &&
            !usuario.getRole().name().equals("ADMINISTRADOR") &&
            !usuario.getRole().name().equals("DIRETORIA")) {
            throw new RuntimeException("Você não tem permissão para deletar este recado");
        }

        notificacaoService.ocultarPorReferencia("RECADO", id);
        recadoRepository.deleteById(id);
    }

    private List<Usuario> resolverDestinatariosNotificacao(Recado recado) {
        return usuarioRepository.findByAtivoTrue().stream()
            .filter(usuario -> deveReceberNotificacaoRecado(recado, usuario))
            .collect(Collectors.toList());
    }

    private boolean deveReceberNotificacaoRecado(Recado recado, Usuario usuario) {
        if (usuario == null || usuario.getId() == null || recado.getRemetente() == null) {
            return false;
        }
        if (usuario.getId().equals(recado.getRemetente().getId())) {
            return false;
        }

        List<Long> especificos = recado.getDestinatariosEspecificos();
        if (especificos != null && !especificos.isEmpty()) {
            return especificos.contains(usuario.getId()) || responsavelDeAlunoEspecifico(usuario, especificos);
        }

        if (!perfilDestinatario(recado, usuario)) {
            return false;
        }

        return usuarioAtendeFiltroTurma(recado, usuario);
    }

    private boolean perfilDestinatario(Recado recado, Usuario usuario) {
        List<String> destinatarios = recado.getDestinatarios();
        if (destinatarios == null || destinatarios.isEmpty() || destinatarios.contains("TODOS")) {
            return true;
        }

        String role = usuario.getRole().name();
        if (destinatarios.contains(role)) {
            return true;
        }

        return role.equals("RESPONSAVEL") && destinatarios.contains("ALUNO");
    }

    private boolean usuarioAtendeFiltroTurma(Recado recado, Usuario usuario) {
        List<String> turmas = recado.getTurmasDestinatarias();
        if (turmas == null || turmas.isEmpty()) {
            return true;
        }

        String role = usuario.getRole().name();
        if (role.equals("ALUNO")) {
            return usuario.getTurma() != null && turmas.contains(usuario.getTurma());
        }
        if (role.equals("RESPONSAVEL")) {
            return usuario.getAlunosVinculados() != null && usuario.getAlunosVinculados().stream()
                .anyMatch(aluno -> aluno.getTurma() != null && turmas.contains(aluno.getTurma()));
        }
        if (role.equals("PROFESSOR")) {
            return usuario.getTurmas() != null && usuario.getTurmas().stream().anyMatch(turmas::contains);
        }
        return true;
    }

    private boolean responsavelDeAlunoEspecifico(Usuario usuario, List<Long> alunosIds) {
        if (!usuario.getRole().name().equals("RESPONSAVEL") || usuario.getAlunosVinculados() == null) {
            return false;
        }

        return usuario.getAlunosVinculados().stream()
            .anyMatch(aluno -> aluno.getId() != null && alunosIds.contains(aluno.getId()));
    }

    private RecadoDTO convertToDTO(Recado recado, Usuario usuario) {
        RecadoDTO dto = modelMapper.map(recado, RecadoDTO.class);
        dto.setRemetente(recado.getRemetente().getNome());
        dto.setRemetenteId(recado.getRemetente().getId());
        dto.setTurmasDestinatarias(recado.getTurmasDestinatarias());

        LeituraRecado leitura = leituraRecadoRepository
            .findByRecadoIdAndUsuarioId(recado.getId(), usuario.getId())
            .orElse(null);

        dto.setLido(leitura != null && leitura.getConfirmada());
        if (leitura != null) {
            dto.setDataLeitura(leitura.getDataLeitura());
        }

        return dto;
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
