package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.config.JwtUtil;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return Map.of(
                "token", token,
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole());
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Map.of("valid", false);
        }

        String token = authorizationHeader.substring(7);
        boolean valid = jwtUtil.isTokenValid(token);

        return Map.of("valid", valid);
    }
}