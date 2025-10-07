package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.RecadoDTO;
import com.educonnect.EduConnect.model.LeituraRecado;
import com.educonnect.EduConnect.model.Recado;
import com.educonnect.EduConnect.model.Usuario;
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
    
    public List<RecadoDTO> getRecadosParaUsuario() {
        Usuario usuario = getUsuarioLogado();
        List<Recado> recados = recadoRepository.findAllByOrderByDataEnvioDesc();
        
        return recados.stream()
            .filter(recado -> podeVerRecado(recado, usuario))
            .map(recado -> convertToDTO(recado, usuario))
            .collect(Collectors.toList());
    }
    
    private boolean podeVerRecado(Recado recado, Usuario usuario) {
        // Verificar se é destinatário específico (prioridade máxima)
        if (recado.getDestinatariosEspecificos() != null && !recado.getDestinatariosEspecificos().isEmpty()) {
            // Se o usuário está na lista de destinatários específicos
            if (recado.getDestinatariosEspecificos().contains(usuario.getId())) {
                return true;
            }
            
            // Se o usuário é RESPONSAVEL e algum de seus filhos está na lista
            if (usuario.getRole().name().equals("RESPONSAVEL") && usuario.getAlunosVinculados() != null) {
                for (Usuario filho : usuario.getAlunosVinculados()) {
                    if (recado.getDestinatariosEspecificos().contains(filho.getId())) {
                        return true;
                    }
                }
            }
            
            // ADMINISTRADOR e DIRETORIA veem TUDO (mesmo específicos)
            if (usuario.getRole().name().equals("ADMINISTRADOR") || 
                usuario.getRole().name().equals("DIRETORIA")) {
                return true;
            }
            
            // PROFESSOR vê recados específicos de seus alunos
            if (usuario.getRole().name().equals("PROFESSOR")) {
                return true; // Professor pode ver todos os recados específicos para acompanhamento
            }
            
            // Se não atende nenhuma condição, não pode ver
            return false;
        }
        
        // Se não tem destinatários definidos, todos podem ver (compatibilidade)
        if (recado.getDestinatarios() == null || recado.getDestinatarios().isEmpty()) {
            return true;
        }
        
        // Se tem "TODOS" na lista, todos podem ver
        if (recado.getDestinatarios().contains("TODOS")) {
            return true;
        }
        
        String roleUsuario = usuario.getRole().name();
        
        // ADMINISTRADOR e DIRETORIA veem TUDO
        if (roleUsuario.equals("ADMINISTRADOR") || roleUsuario.equals("DIRETORIA")) {
            return true;
        }
        
        // PROFESSOR vê: seus próprios + ALUNO + RESPONSAVEL
        if (roleUsuario.equals("PROFESSOR")) {
            return recado.getDestinatarios().contains("PROFESSOR") ||
                   recado.getDestinatarios().contains("ALUNO") ||
                   recado.getDestinatarios().contains("RESPONSAVEL");
        }
        
        // RESPONSAVEL vê: seus próprios + ALUNO
        if (roleUsuario.equals("RESPONSAVEL")) {
            return recado.getDestinatarios().contains("RESPONSAVEL") ||
                   recado.getDestinatarios().contains("ALUNO");
        }
        
        // ALUNO vê apenas: seus próprios
        if (roleUsuario.equals("ALUNO")) {
            return recado.getDestinatarios().contains("ALUNO");
        }
        
        // Padrão: verificar se a role está na lista
        return recado.getDestinatarios().contains(roleUsuario);
    }
    
    public RecadoDTO getRecadoById(Long id) {
        Usuario usuario = getUsuarioLogado();
        Recado recado = recadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recado não encontrado"));
        
        return convertToDTO(recado, usuario);
    }
    
    public RecadoDTO criarRecado(Recado recado) {
        Usuario remetente = getUsuarioLogado();
        recado.setRemetente(remetente);
        
        Recado savedRecado = recadoRepository.save(recado);
        return convertToDTO(savedRecado, remetente);
    }
    
    public void confirmarLeitura(Long recadoId) {
        Usuario usuario = getUsuarioLogado();
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
        
        // Verificar se o usuário é o remetente ou tem permissão
        if (!recado.getRemetente().getId().equals(usuario.getId()) && 
            !usuario.getRole().name().equals("ADMINISTRADOR") &&
            !usuario.getRole().name().equals("DIRETORIA")) {
            throw new RuntimeException("Você não tem permissão para editar este recado");
        }
        
        // Atualizar campos
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
        
        // Verificar se o usuário é o remetente ou tem permissão
        if (!recado.getRemetente().getId().equals(usuario.getId()) && 
            !usuario.getRole().name().equals("ADMINISTRADOR") &&
            !usuario.getRole().name().equals("DIRETORIA")) {
            throw new RuntimeException("Você não tem permissão para deletar este recado");
        }
        
        recadoRepository.deleteById(id);
    }
    
    private RecadoDTO convertToDTO(Recado recado, Usuario usuario) {
        RecadoDTO dto = modelMapper.map(recado, RecadoDTO.class);
        dto.setRemetente(recado.getRemetente().getNome());
        dto.setRemetenteId(recado.getRemetente().getId());
        
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

