package com.worldcup.api.controller;

import com.worldcup.api.dto.request.LoginRequest;
import com.worldcup.api.dto.request.RegisterRequest;
import com.worldcup.api.dto.response.AuthResponse;
import com.worldcup.api.entity.Usuario;
import com.worldcup.api.entity.enums.Role;
import com.worldcup.api.repository.UsuarioRepository;
import com.worldcup.api.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        String token = jwtService.generateToken(usuario);
        return ResponseEntity.ok(new AuthResponse(token, usuario.getNome(), usuario.getEmail(),
                usuario.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(req.nome());
        usuario.setEmail(req.email());
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));
        usuario.setRole(Role.USER);
        usuarioRepository.save(usuario);
        String token = jwtService.generateToken(usuario);
        return ResponseEntity.ok(new AuthResponse(token, usuario.getNome(), usuario.getEmail(),
                usuario.getRole().name()));
    }
}
