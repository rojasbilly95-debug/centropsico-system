package com.centropsicologico.sistema.config;

import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuditLogService auditLogService;

    public SecurityConfig(
            UserRepository userRepository,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.auditLogService = auditLogService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // RUTAS PÚBLICAS
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/portal.html",
                                "/css/**",
                                "/js/**",
                                "/components/**",
                                "/img/**",
                                "/uploads/**",
                                "/favicon.ico",
                                "/api/auth/**",
                                "/api/public/**",
                                "/ws/**"
                        )
                        .permitAll()

                        // PERFIL
                        .requestMatchers("/api/profile/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // DASHBOARD
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // AUDITORÍA / MOVIMIENTOS
                        .requestMatchers("/api/audit-logs/**")
                        .hasAuthority("ADMIN")

                        // USUARIOS
                        .requestMatchers("/api/users/**")
                        .hasAuthority("ADMIN")

                        // NOTIFICACIONES DIRECTAS DEL ADMIN A USUARIOS
                        .requestMatchers("/api/admin-notifications/**")
                        .hasAuthority("ADMIN")

                        // PROMOCIONES DEL PORTAL
                        .requestMatchers("/api/promotions/**")
                        .hasAuthority("ADMIN")

                        // PRE-RESERVAS
                        .requestMatchers("/api/leads/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA")

                        // PSICÓLOGOS - CONSULTA
                        .requestMatchers(HttpMethod.GET, "/api/psychologists/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // SERVICIOS - CONSULTA
                        .requestMatchers(HttpMethod.GET, "/api/services/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // PSICÓLOGOS - ADMINISTRACIÓN
                        .requestMatchers("/api/psychologists/**")
                        .hasAuthority("ADMIN")

                        // SERVICIOS - ADMINISTRACIÓN
                        .requestMatchers("/api/services/**")
                        .hasAuthority("ADMIN")

                        // FINANZAS
                        .requestMatchers("/api/finances/**")
                        .hasAuthority("ADMIN")

                        // REPORTES
                        .requestMatchers("/api/reports/**")
                        .hasAuthority("ADMIN")

                        // PACIENTES
                        .requestMatchers("/api/patients/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA")

                        // DISPONIBILIDAD - CONSULTA
                        .requestMatchers(HttpMethod.GET, "/api/psychologist-availabilities/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // DISPONIBILIDAD - ADMINISTRACIÓN
                        .requestMatchers("/api/psychologist-availabilities/**")
                        .hasAuthority("ADMIN")

                        // NOTIFICACIONES
                        .requestMatchers("/api/notifications/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // CITAS
                        .requestMatchers("/api/appointments/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        // HISTORIA CLÍNICA
                        .requestMatchers("/api/clinical-history/**")
                        .hasAnyAuthority("ADMIN", "PSICOLOGO")

                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic.disable())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            auditUnauthorizedRequest(request);

                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"No autorizado\"}");
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            auditAccessDenied(request);

                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Acceso denegado\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            com.centropsicologico.sistema.entity.User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Boolean.TRUE.equals(user.getActive()),
                    true,
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority(user.getRole()))
            );
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    private void auditUnauthorizedRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")) {
            return;
        }

        if (shouldSkipUnauthorizedAudit(uri)) {
            return;
        }

        auditLogService.recordSecurity(
                "SEGURIDAD",
                "NO AUTORIZADO",
                "Endpoint",
                null,
                "Intento de acceso sin autenticación válida a "
                        + request.getMethod()
                        + " "
                        + uri
                        + ". "
                        + getClientInfo(request),
                "NO_AUTENTICADO",
                "NO_AUTENTICADO",
                "WARNING",
                false
        );
    }

    private void auditAccessDenied(HttpServletRequest request) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication != null && authentication.getName() != null
                ? authentication.getName()
                : "desconocido";

        String role = authentication != null && authentication.getAuthorities() != null
                ? authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .orElse("SIN_ROL")
                : "SIN_ROL";

        String uri = request.getRequestURI();
        boolean sensitive = isSensitiveEndpoint(uri);

        auditLogService.recordSecurity(
                "SEGURIDAD",
                sensitive ? "ACCESO DENEGADO CRÍTICO" : "ACCESO DENEGADO",
                "Endpoint",
                null,
                "El usuario intentó acceder sin permisos a "
                        + request.getMethod()
                        + " "
                        + uri
                        + ". "
                        + getClientInfo(request),
                email,
                role,
                sensitive ? "CRITICAL" : "WARNING",
                sensitive
        );
    }

    private boolean shouldSkipUnauthorizedAudit(String uri) {
        return uri.startsWith("/api/auth/validate")
                || uri.startsWith("/api/notifications")
                || uri.startsWith("/api/dashboard");
    }

    private boolean isSensitiveEndpoint(String uri) {
        return uri.startsWith("/api/users")
                || uri.startsWith("/api/admin-notifications")
                || uri.startsWith("/api/promotions")
                || uri.startsWith("/api/finances")
                || uri.startsWith("/api/reports")
                || uri.startsWith("/api/audit-logs")
                || uri.startsWith("/api/profile")
                || uri.startsWith("/api/services")
                || uri.startsWith("/api/psychologists")
                || uri.startsWith("/api/clinical-history");
    }

    private String getClientInfo(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "localhost";
        }

        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "No identificado";
        }

        userAgent = simplifyUserAgent(userAgent);

        return "IP: " + ip + ". Navegador: " + userAgent;
    }

    private String simplifyUserAgent(String userAgent) {
        String browser = "Navegador desconocido";
        String os = "Sistema desconocido";

        if (userAgent.contains("Windows NT 10.0")) {
            os = "Windows 10/11";
        } else if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        } else if (userAgent.contains("Mac OS")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        }

        if (userAgent.contains("Edg/")) {
            browser = "Microsoft Edge";
        } else if (userAgent.contains("Chrome/")) {
            browser = "Google Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Mozilla Firefox";
        } else if (userAgent.contains("Safari/")) {
            browser = "Safari";
        }

        return browser + " en " + os;
    }
}