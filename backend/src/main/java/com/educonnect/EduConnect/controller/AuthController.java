package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.LoginRequest;
import com.educonnect.EduConnect.dto.LoginResponse;
import com.educonnect.EduConnect.dto.UsuarioDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody Usuario usuario) {
        UsuarioDTO usuarioDTO = authService.register(usuario);
        return ResponseEntity.ok(usuarioDTO);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        UsuarioDTO usuarioDTO = authService.getUserInfo(usuario);
        return ResponseEntity.ok(usuarioDTO);
    }
}

