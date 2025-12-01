package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.LoginRequest;
import com.educonnect.EduConnect.dto.LoginResponse;
import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.repository.UsuarioRepository;
import com.educonnect.EduConnect.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
    public LoginResponse login(LoginRequest request) {
        System.out.println("🔍 DEBUG - Tentando login com email: " + request.getEmail());
        
        // Verificar se usuário existe
        var userOptional = usuarioRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            System.out.println("❌ Usuário não encontrado no banco: " + request.getEmail());
            throw new RuntimeException("E-mail ou senha inválidos");
        }
        
        Usuario usuarioNoBanco = userOptional.get();
        System.out.println("✅ Usuário encontrado: " + usuarioNoBanco.getEmail());
        System.out.println("🔐 Senha no banco (hash): " + usuarioNoBanco.getPassword().substring(0, 20) + "...");
        System.out.println("🔐 Senha recebida: " + request.getPassword());
        System.out.println("👤 Usuário ativo: " + usuarioNoBanco.getAtivo());
        
        // Tentar autenticar
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtUtil.generateToken(usuario);
            
            // Mapear manualmente para garantir que escolaId seja incluído
            UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
            
            if (usuario.getEscola() != null) {
                usuarioDTO.setEscolaId(usuario.getEscola().getId());
                usuarioDTO.setEscolaNome(usuario.getEscola().getNome());
            }
            
            if (usuario.getResponsavel() != null) {
                usuarioDTO.setResponsavelId(usuario.getResponsavel().getId());
                usuarioDTO.setResponsavelNome(usuario.getResponsavel().getNome());
            }
            
            System.out.println("✅ Login bem-sucedido para: " + usuario.getEmail());
            return new LoginResponse(token, usuarioDTO);
        } catch (Exception e) {
            System.out.println("❌ Falha na autenticação: " + e.getMessage());
            throw e;
        }
    }
    
    public UsuarioDTO register(Usuario usuario) {
        System.out.println("🔍 DEBUG - Registrando usuário: " + usuario.getEmail());
        
        // Validar email duplicado
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }
        
        // Validar CPF duplicado
        if (usuario.getCpf() != null && !usuario.getCpf().isEmpty() 
            && usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }
        
        // Validar força da senha
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            UsuarioService.validarForcaSenha(usuario.getPassword());
        }
        
        System.out.println("🔐 Senha antes de criptografar: " + usuario.getPassword());
        String senhaOriginal = usuario.getPassword();
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        System.out.println("🔐 Senha criptografada: " + usuario.getPassword().substring(0, 20) + "...");
        usuario.setAtivo(true);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        System.out.println("✅ Usuário salvo com ID: " + savedUsuario.getId());
        System.out.println("✅ Email salvo: " + savedUsuario.getEmail());
        System.out.println("✅ Role salvo: " + savedUsuario.getRole());
        System.out.println("✅ Ativo: " + savedUsuario.getAtivo());
        
        return modelMapper.map(savedUsuario, UsuarioDTO.class);
    }
    
    public UsuarioDTO getUserInfo(Usuario usuario) {
        // Buscar usuário atualizado do banco
        Usuario usuarioAtualizado = usuarioRepository.findByEmail(usuario.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Mapear manualmente para garantir que escolaId seja incluído
        UsuarioDTO dto = modelMapper.map(usuarioAtualizado, UsuarioDTO.class);
        
        if (usuarioAtualizado.getEscola() != null) {
            dto.setEscolaId(usuarioAtualizado.getEscola().getId());
            dto.setEscolaNome(usuarioAtualizado.getEscola().getNome());
        }
        
        if (usuarioAtualizado.getResponsavel() != null) {
            dto.setResponsavelId(usuarioAtualizado.getResponsavel().getId());
            dto.setResponsavelNome(usuarioAtualizado.getResponsavel().getNome());
        }
        
        return dto;
    }
}

