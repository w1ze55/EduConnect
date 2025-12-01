package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.AtividadeResumoDTO;
import com.educonnect.EduConnect.dto.DashboardResponseDTO;
import com.educonnect.EduConnect.dto.DashboardStatsDTO;
import com.educonnect.EduConnect.dto.EventoResumoDTO;
import com.educonnect.EduConnect.dto.RecadoResumoDTO;
import com.educonnect.EduConnect.model.Atividade;
import com.educonnect.EduConnect.model.Evento;
import com.educonnect.EduConnect.model.Recado;
import com.educonnect.EduConnect.model.Turma;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.AtividadeRepository;
import com.educonnect.EduConnect.repository.EventoRepository;
import com.educonnect.EduConnect.repository.RecadoRepository;
import com.educonnect.EduConnect.repository.TurmaRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RecadoRepository recadoRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;

    public DashboardResponseDTO getResumo(Long escolaId, Long turmaId, Long usuarioId,
                                          String datePreset, String startDate, String endDate) {
        Usuario usuarioLogado = getUsuarioLogado();

        Usuario usuarioFiltro = usuarioId != null ? usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário do filtro não encontrado")) : null;

        Turma turmaFiltro = turmaId != null ? turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma do filtro não encontrada")) : null;

        validarPermissoes(usuarioLogado, escolaId, turmaFiltro, usuarioFiltro);

        Long escolaContexto = escolaId != null
                ? escolaId
                : (turmaFiltro != null && turmaFiltro.getEscola() != null
                    ? turmaFiltro.getEscola().getId()
                    : resolveEscolaDoUsuario(usuarioLogado));
        String turmaNomeFiltro = turmaFiltro != null ? turmaFiltro.getNome() : null;

        List<Usuario> usuariosContexto = resolverUsuariosContexto(usuarioLogado, escolaContexto, turmaFiltro, usuarioFiltro);

        LocalDateTime[] range = resolverRangeDatas(datePreset, startDate, endDate);

        List<RecadoResumoDTO> recados = filtrarRecados(usuariosContexto, escolaContexto, range[0], range[1]);
        List<AtividadeResumoDTO> atividades = filtrarAtividades(usuariosContexto, escolaContexto, turmaNomeFiltro, range[0], range[1]);
        List<EventoResumoDTO> eventos = filtrarEventos(usuariosContexto, escolaContexto, range[0], range[1]);

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setStats(new DashboardStatsDTO(recados.size(), atividades.size(), eventos.size()));
        response.setRecadosRecentes(recados.stream().limit(5).collect(Collectors.toList()));
        response.setAtividadesPendentes(atividades.stream()
                .sorted(Comparator.comparing(AtividadeResumoDTO::getDataEntrega, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList()));
        response.setProximosEventos(eventos.stream().limit(6).collect(Collectors.toList()));

        return response;
    }

    private List<RecadoResumoDTO> filtrarRecados(List<Usuario> usuariosContexto, Long escolaFiltro,
                                                 LocalDateTime inicio, LocalDateTime fim) {
        return recadoRepository.findAllByOrderByDataEnvioDesc().stream()
                .filter(recado -> escolaFiltro == null || pertenceAEscola(recado.getRemetente(), escolaFiltro))
                .filter(recado -> dentroDoRange(recado.getDataEnvio(), inicio, fim))
                .filter(recado -> usuariosContexto.stream().anyMatch(usuario -> podeVerRecado(recado, usuario)))
                .map(this::converterRecado)
                .collect(Collectors.toList());
    }

    private List<AtividadeResumoDTO> filtrarAtividades(List<Usuario> usuariosContexto, Long escolaFiltro,
                                                       String turmaFiltro, LocalDateTime inicio, LocalDateTime fim) {
        return atividadeRepository.findAll().stream()
                .filter(atividade -> escolaFiltro == null || pertenceAEscola(atividade.getProfessor(), escolaFiltro))
                .filter(atividade -> turmaFiltro == null || (atividade.getTurma() != null && atividade.getTurma().equalsIgnoreCase(turmaFiltro)))
                .filter(atividade -> dentroDoRange(atividade.getDataEntrega(), inicio, fim))
                .filter(atividade -> usuariosContexto.stream().anyMatch(usuario -> podeVerAtividade(atividade, usuario)))
                .sorted(Comparator.comparing(Atividade::getDataEntrega, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::converterAtividade)
                .collect(Collectors.toList());
    }

    private List<EventoResumoDTO> filtrarEventos(List<Usuario> usuariosContexto, Long escolaContexto,
                                                 LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime baseInicio = inicio != null ? inicio : LocalDateTime.now().minusDays(1);

        return eventoRepository.findProximosEventos(baseInicio).stream()
                .filter(evento -> escolaContexto == null || pertenceAEscola(evento.getCriador(), escolaContexto))
                .filter(evento -> dentroDoRange(evento.getDataInicio(), inicio, fim))
                .filter(evento -> usuariosContexto.stream().anyMatch(usuario -> podeVerEvento(evento, usuario)))
                .sorted(Comparator.comparing(Evento::getDataInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::converterEvento)
                .collect(Collectors.toList());
    }

    private void validarPermissoes(Usuario usuarioLogado, Long escolaId, Turma turmaFiltro, Usuario usuarioFiltro) {
        Role role = usuarioLogado.getRole();

        if (role == Role.ADMINISTRADOR) {
            return;
        }

        if (role == Role.DIRETORIA || role == Role.PROFESSOR) {
            Long escolaDoUsuario = resolveEscolaDoUsuario(usuarioLogado);

            if (escolaId != null && !escolaId.equals(escolaDoUsuario)) {
                throw new RuntimeException("Você não tem permissão para visualizar dados de outra escola");
            }

            if (turmaFiltro != null && turmaFiltro.getEscola() != null &&
                    !turmaFiltro.getEscola().getId().equals(escolaDoUsuario)) {
                throw new RuntimeException("Você não tem permissão para visualizar esta turma");
            }

            if (usuarioFiltro != null && usuarioFiltro.getEscola() != null &&
                    !usuarioFiltro.getEscola().getId().equals(escolaDoUsuario)) {
                throw new RuntimeException("Você não tem permissão para visualizar usuários de outra escola");
            }
        } else {
            if (escolaId != null || turmaFiltro != null || usuarioFiltro != null) {
                throw new RuntimeException("Seu perfil não permite aplicar filtros no dashboard");
            }
        }
    }

    private List<Usuario> resolverUsuariosContexto(Usuario usuarioLogado, Long escolaId, Turma turmaFiltro, Usuario usuarioFiltro) {
        if (usuarioFiltro != null) {
            return expandirUsuario(usuarioFiltro);
        }

        if (turmaFiltro != null) {
            List<Usuario> usuarios = new ArrayList<>();
            if (turmaFiltro.getProfessores() != null) {
                usuarios.addAll(turmaFiltro.getProfessores());
            }
            if (turmaFiltro.getAlunos() != null) {
                usuarios.addAll(turmaFiltro.getAlunos());
                turmaFiltro.getAlunos().forEach(aluno -> {
                    if (aluno.getResponsavel() != null) {
                        usuarios.add(aluno.getResponsavel());
                    }
                });
            }
            return distinctUsuarios(usuarios);
        }

        if (escolaId != null) {
            List<Usuario> usuarios = usuarioRepository.findByEscolaId(escolaId);
            return distinctUsuarios(expandirResponsaveis(usuarios));
        }

        return expandirUsuario(usuarioLogado);
    }

    private boolean podeVerRecado(Recado recado, Usuario usuario) {
        // Mesma lógica do RecadoService para manter coerência
        if (recado.getDestinatariosEspecificos() != null && !recado.getDestinatariosEspecificos().isEmpty()) {
            if (recado.getDestinatariosEspecificos().contains(usuario.getId())) {
                return true;
            }

            if (usuario.getRole() == Role.RESPONSAVEL && usuario.getAlunosVinculados() != null) {
                for (Usuario filho : usuario.getAlunosVinculados()) {
                    if (recado.getDestinatariosEspecificos().contains(filho.getId())) {
                        return true;
                    }
                }
            }

            if (usuario.getRole() == Role.ADMINISTRADOR || usuario.getRole() == Role.DIRETORIA) {
                return true;
            }

            if (usuario.getRole() == Role.PROFESSOR) {
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

    private boolean podeVerAtividade(Atividade atividade, Usuario usuario) {
        Role role = usuario.getRole();

        if (role == Role.ADMINISTRADOR) {
            return true;
        }

        if (role == Role.DIRETORIA) {
            return usuario.getEscola() != null &&
                    atividade.getProfessor().getEscola() != null &&
                    usuario.getEscola().getId().equals(atividade.getProfessor().getEscola().getId());
        }

        if (role == Role.PROFESSOR) {
            return atividade.getProfessor().getId().equals(usuario.getId());
        }

        if (role == Role.ALUNO) {
            return usuario.getTurma() != null && usuario.getTurma().equals(atividade.getTurma());
        }

        if (role == Role.RESPONSAVEL) {
            if (usuario.getAlunosVinculados() == null || usuario.getAlunosVinculados().isEmpty()) {
                return false;
            }

            return usuario.getAlunosVinculados().stream()
                    .anyMatch(aluno -> aluno.getTurma() != null && aluno.getTurma().equals(atividade.getTurma()));
        }

        return false;
    }

    private boolean podeVerEvento(Evento evento, Usuario usuario) {
        if (usuario.getRole() == Role.ADMINISTRADOR) {
            return true;
        }

        if (evento.getCriador() == null) {
            return true;
        }

        if (usuario.getRole() == Role.DIRETORIA || usuario.getRole() == Role.PROFESSOR) {
            return usuario.getEscola() != null &&
                    evento.getCriador().getEscola() != null &&
                    usuario.getEscola().getId().equals(evento.getCriador().getEscola().getId());
        }

        return true;
    }

    private RecadoResumoDTO converterRecado(Recado recado) {
        RecadoResumoDTO dto = new RecadoResumoDTO();
        dto.setId(recado.getId());
        dto.setTitulo(recado.getTitulo());
        dto.setConteudo(recado.getConteudo());
        dto.setRemetente(recado.getRemetente() != null ? recado.getRemetente().getNome() : null);
        dto.setRemetenteId(recado.getRemetente() != null ? recado.getRemetente().getId() : null);
        dto.setCategoria(recado.getCategoria() != null ? recado.getCategoria().name() : null);
        dto.setImportante(Boolean.TRUE.equals(recado.getImportante()));
        dto.setLido(false);
        dto.setDataEnvio(recado.getDataEnvio());
        return dto;
    }

    private AtividadeResumoDTO converterAtividade(Atividade atividade) {
        AtividadeResumoDTO dto = new AtividadeResumoDTO();
        dto.setId(atividade.getId());
        dto.setTitulo(atividade.getTitulo());
        dto.setDisciplina(atividade.getDisciplina());
        dto.setTurma(atividade.getTurma());
        dto.setProfessorId(atividade.getProfessor() != null ? atividade.getProfessor().getId() : null);
        dto.setProfessorNome(atividade.getProfessor() != null ? atividade.getProfessor().getNome() : null);
        dto.setDataEntrega(atividade.getDataEntrega());
        return dto;
    }

    private EventoResumoDTO converterEvento(Evento evento) {
        EventoResumoDTO dto = new EventoResumoDTO();
        dto.setId(evento.getId());
        dto.setTitulo(evento.getTitulo());
        dto.setTipo(evento.getTipo());
        dto.setCriadorId(evento.getCriador() != null ? evento.getCriador().getId() : null);
        dto.setCriadorNome(evento.getCriador() != null ? evento.getCriador().getNome() : null);
        dto.setEscolaId(evento.getCriador() != null && evento.getCriador().getEscola() != null
                ? evento.getCriador().getEscola().getId()
                : null);
        dto.setDataInicio(evento.getDataInicio());
        dto.setDataFim(evento.getDataFim());
        return dto;
    }

    private List<Usuario> expandirUsuario(Usuario usuario) {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        if (usuario.getRole() == Role.RESPONSAVEL && usuario.getAlunosVinculados() != null) {
            usuarios.addAll(usuario.getAlunosVinculados());
        }

        return distinctUsuarios(usuarios);
    }

    private List<Usuario> expandirResponsaveis(List<Usuario> usuarios) {
        List<Usuario> resultado = new ArrayList<>(usuarios);

        usuarios.stream()
                .filter(u -> u.getRole() == Role.RESPONSAVEL && u.getAlunosVinculados() != null)
                .forEach(u -> resultado.addAll(u.getAlunosVinculados()));

        return resultado;
    }

    private Long resolveEscolaDoUsuario(Usuario usuario) {
        return usuario.getEscola() != null ? usuario.getEscola().getId() : null;
    }

    private boolean pertenceAEscola(Usuario usuario, Long escolaId) {
        return usuario != null &&
                usuario.getEscola() != null &&
                usuario.getEscola().getId().equals(escolaId);
    }

    private boolean dentroDoRange(LocalDateTime data, LocalDateTime inicio, LocalDateTime fim) {
        if (data == null) return false;
        if (inicio != null && data.isBefore(inicio)) return false;
        if (fim != null && data.isAfter(fim)) return false;
        return true;
    }

    private LocalDateTime[] resolverRangeDatas(String preset, String startDate, String endDate) {
        LocalDateTime inicio = null;
        LocalDateTime fim = null;

        if (preset != null && !preset.isBlank()) {
            LocalDate hoje = LocalDate.now();
            switch (preset.toLowerCase()) {
                case "hoje":
                    inicio = hoje.atStartOfDay();
                    fim = hoje.atTime(LocalTime.MAX);
                    break;
                case "ontem":
                    LocalDate ontem = hoje.minusDays(1);
                    inicio = ontem.atStartOfDay();
                    fim = ontem.atTime(LocalTime.MAX);
                    break;
                case "essa_semana":
                    LocalDate segunda = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
                    inicio = segunda.atStartOfDay();
                    fim = inicio.plusDays(6).with(LocalTime.MAX);
                    break;
                case "esse_mes":
                    LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);
                    inicio = primeiroDiaMes.atStartOfDay();
                    fim = primeiroDiaMes.plusMonths(1).minusDays(1).atTime(LocalTime.MAX);
                    break;
                case "ultimo_mes":
                    LocalDate primeiroDiaUltimoMes = hoje.minusMonths(1).withDayOfMonth(1);
                    inicio = primeiroDiaUltimoMes.atStartOfDay();
                    fim = primeiroDiaUltimoMes.plusMonths(1).minusDays(1).atTime(LocalTime.MAX);
                    break;
                default:
                    break;
            }
        }

        if (startDate != null && !startDate.isBlank()) {
            inicio = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            fim = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        return new LocalDateTime[]{inicio, fim};
    }

    private List<Usuario> distinctUsuarios(List<Usuario> usuarios) {
        Map<Long, Usuario> usuariosUnicos = usuarios.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(
                        Usuario::getId,
                        Function.identity(),
                        (u1, u2) -> u1,
                        LinkedHashMap::new
                ));

        return new ArrayList<>(usuariosUnicos.values());
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
