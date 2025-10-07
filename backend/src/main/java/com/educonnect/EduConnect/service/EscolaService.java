package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.AtribuirDiretorDTO;
import com.educonnect.EduConnect.dto.EscolaCreateDTO;
import com.educonnect.EduConnect.dto.EscolaDTO;
import com.educonnect.EduConnect.model.Escola;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.EscolaRepository;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EscolaService {
    
    private final EscolaRepository escolaRepository;
    private final UsuarioRepository usuarioRepository;
    
    @Transactional(readOnly = true)
    public List<EscolaDTO> listarTodas() {
        return escolaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EscolaDTO> listarAtivas() {
        return escolaRepository.findByAtivoTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public EscolaDTO buscarPorId(Long id) {
        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
        return convertToDTO(escola);
    }
    
    @Transactional
    public EscolaDTO criar(EscolaCreateDTO dto) {
        if (escolaRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Já existe uma escola cadastrada com este e-mail");
        }
        
        Escola escola = new Escola();
        escola.setNome(dto.getNome());
        escola.setEndereco(dto.getEndereco());
        escola.setTelefone(dto.getTelefone());
        escola.setEmail(dto.getEmail());
        escola.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        escola = escolaRepository.save(escola);
        return convertToDTO(escola);
    }
    
    @Transactional
    public EscolaDTO atualizar(Long id, EscolaCreateDTO dto) {
        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
        
        // Verificar se o email já está em uso por outra escola
        if (!escola.getEmail().equals(dto.getEmail()) && escolaRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Já existe uma escola cadastrada com este e-mail");
        }
        
        escola.setNome(dto.getNome());
        escola.setEndereco(dto.getEndereco());
        escola.setTelefone(dto.getTelefone());
        escola.setEmail(dto.getEmail());
        escola.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        escola = escolaRepository.save(escola);
        return convertToDTO(escola);
    }
    
    @Transactional
    public void deletar(Long id) {
        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
        
        // Verificar se há usuários vinculados
        if (escola.getUsuarios() != null && !escola.getUsuarios().isEmpty()) {
            throw new RuntimeException("Não é possível excluir uma escola com usuários vinculados");
        }
        
        escolaRepository.delete(escola);
    }
    
    @Transactional
    public EscolaDTO atribuirDiretor(AtribuirDiretorDTO dto) {
        Escola escola = escolaRepository.findById(dto.getEscolaId())
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
        
        Usuario diretor = usuarioRepository.findById(dto.getDiretorId())
                .orElseThrow(() -> new RuntimeException("Diretor não encontrado"));
        
        if (diretor.getRole() != Role.DIRETORIA) {
            throw new RuntimeException("O usuário selecionado não possui o perfil de Diretor");
        }
        
        if (!diretor.getAtivo()) {
            throw new RuntimeException("O diretor selecionado está inativo");
        }
        
        escola.setDiretor(diretor);
        diretor.setEscola(escola);
        
        escolaRepository.save(escola);
        usuarioRepository.save(diretor);
        
        return convertToDTO(escola);
    }
    
    @Transactional
    public EscolaDTO removerDiretor(Long escolaId) {
        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
        
        if (escola.getDiretor() != null) {
            Usuario diretor = escola.getDiretor();
            diretor.setEscola(null);
            usuarioRepository.save(diretor);
            escola.setDiretor(null);
        }
        
        escola = escolaRepository.save(escola);
        return convertToDTO(escola);
    }
    
    private EscolaDTO convertToDTO(Escola escola) {
        EscolaDTO dto = new EscolaDTO();
        dto.setId(escola.getId());
        dto.setNome(escola.getNome());
        dto.setEndereco(escola.getEndereco());
        dto.setTelefone(escola.getTelefone());
        dto.setEmail(escola.getEmail());
        dto.setAtivo(escola.getAtivo());
        dto.setDataCadastro(escola.getDataCadastro());
        
        if (escola.getDiretor() != null) {
            EscolaDTO.DiretorSimplificadoDTO diretorDTO = new EscolaDTO.DiretorSimplificadoDTO();
            diretorDTO.setId(escola.getDiretor().getId());
            diretorDTO.setNome(escola.getDiretor().getNome());
            diretorDTO.setEmail(escola.getDiretor().getEmail());
            dto.setDiretor(diretorDTO);
        }
        
        return dto;
    }
}
