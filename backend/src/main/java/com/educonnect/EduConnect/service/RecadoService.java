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
            .map(recado -> convertToDTO(recado, usuario))
            .collect(Collectors.toList());
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

