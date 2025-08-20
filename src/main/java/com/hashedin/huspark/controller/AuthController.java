package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.AuthDtos.AuthResponse;
import com.hashedin.huspark.dto.AuthDtos.LoginRequest;
import com.hashedin.huspark.dto.AuthDtos.RegisterRequest;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.hashedin.huspark.service.AuditService;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.MEMBER) // always MEMBER on self-register
                .build();
        userRepository.save(user);

        auditService.log("USER_REGISTER", "{\"userId\":" + user.getId() + ",\"email\":\"" + user.getEmail() + "\"}", true);


        String token = jwtService.generateToken(
                user.getEmail(), Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        var user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generateToken(
                user.getEmail(), Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }
}
