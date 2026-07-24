package com.centropsicologico.sistema.config;

import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuditLogService auditLogService;

    /*
     * Puede contener uno o varios dominios separados por coma.
     *
     * Ejemplo en Render:
     * APP_ALLOWED_ORIGINS=https://centropsico-system.onrender.com
     */
    @Value("""
            ${app.security.allowed-origins:
            http://localhost:8080,
            https://centropsico-system.onrender.com}
            """)
    private String allowedOrigins;

    public SecurityConfig(
            UserRepository userRepository,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
        this.auditLogService = auditLogService;
    }

    /*
     * =========================================================
     * CONFIGURACIÓN PRINCIPAL
     * =========================================================
     */

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 * Actualmente utilizas JWT en el encabezado:
                 * Authorization: Bearer ...
                 *
                 * Mientras el JWT no se encuentre en una cookie,
                 * se mantiene CSRF deshabilitado.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * CORS restringido a los dominios configurados.
                 */
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                /*
                 * No se crea una sesión HTTP en el servidor.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Desactiva mecanismos que no utilizamos
                 * porque la autenticación se realiza con JWT.
                 */
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .rememberMe(remember -> remember.disable())
                .requestCache(cache -> cache.disable())

                .authenticationProvider(
                        authenticationProvider()
                )

                /*
                 * =================================================
                 * PERMISOS POR RUTA
                 * =================================================
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Solicitudes previas CORS.
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * Archivos y páginas públicas.
                         */
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/portal.html",
                                "/error",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/components/**",
                                "/img/**"
                        )
                        .permitAll()

                        /*
                         * Temporalmente las imágenes continúan
                         * siendo públicas para no romper <img src>.
                         *
                         * Posteriormente conviene usar un endpoint
                         * protegido o almacenamiento privado.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/uploads/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.HEAD,
                                "/uploads/**"
                        )
                        .permitAll()

                        /*
                         * Autenticación y portal público.
                         *
                         * Se conserva /** porque todavía necesitamos
                         * revisar AuthController y PublicController.
                         */
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**"
                        )
                        .permitAll()

                        /*
                         * El handshake de SockJS continúa público.
                         *
                         * La autenticación de suscripciones STOMP
                         * debe implementarse en WebSocketConfig.
                         */
                        .requestMatchers("/ws/**")
                        .permitAll()

                        /*
                         * PERFIL
                         */
                        .requestMatchers("/api/profile/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * DASHBOARD
                         */
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * AUDITORÍA
                         */
                        .requestMatchers("/api/audit-logs/**")
                        .hasAuthority("ADMIN")

                        /*
                         * USUARIOS
                         */
                        .requestMatchers("/api/users/**")
                        .hasAuthority("ADMIN")

                        /*
                         * NOTIFICACIONES CREADAS POR ADMIN
                         */
                        .requestMatchers(
                                "/api/admin-notifications/**"
                        )
                        .hasAuthority("ADMIN")

                        /*
                         * PROMOCIONES
                         */
                        .requestMatchers("/api/promotions/**")
                        .hasAuthority("ADMIN")

                        /*
                         * PRE-RESERVAS
                         */
                        .requestMatchers("/api/leads/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * PSICÓLOGOS: CONSULTA
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/psychologists/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * PSICÓLOGOS: ADMINISTRACIÓN
                         */
                        .requestMatchers(
                                "/api/psychologists/**"
                        )
                        .hasAuthority("ADMIN")

                        /*
                         * SERVICIOS: CONSULTA
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/services/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * SERVICIOS: ADMINISTRACIÓN
                         */
                        .requestMatchers("/api/services/**")
                        .hasAuthority("ADMIN")

                        /*
                         * FINANZAS
                         */
                        .requestMatchers("/api/finances/**")
                        .hasAuthority("ADMIN")

                        /*
                         * REPORTES
                         */
                        .requestMatchers("/api/reports/**")
                        .hasAuthority("ADMIN")

                        /*
                         * PACIENTES
                         */
                        .requestMatchers("/api/patients/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * DISPONIBILIDAD: CONSULTA
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/psychologist-availabilities/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * DISPONIBILIDAD: ADMINISTRACIÓN
                         */
                        .requestMatchers(
                                "/api/psychologist-availabilities/**"
                        )
                        .hasAuthority("ADMIN")

                        /*
                         * NOTIFICACIONES
                         */
                        .requestMatchers("/api/notifications/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * =========================================
                         * CITAS: CONSULTAS DEL PSICÓLOGO
                         * =========================================
                         */

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/my"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/my/by-date"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * ADMIN Y RECEPCIÓN: CONSULTA GENERAL
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/by-date"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/*"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * CREAR CITA
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/appointments"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * REGISTRAR PAGO
                         *
                         * Esta regla específica se coloca antes
                         * de la regla general de actualización.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/appointments/*/pay"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * CAMBIAR ESTADO
                         *
                         * El servicio debe comprobar que el psicólogo
                         * solo modifique citas que le pertenecen.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/appointments/*/status"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * ACTUALIZACIÓN GENERAL
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/appointments/*"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * ELIMINAR CITA
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/appointments/*"
                        )
                        .hasAuthority("ADMIN")

                        /*
                         * HISTORIA CLÍNICA
                         *
                         * Además del rol, el servicio debe comprobar
                         * que la historia pertenece a un paciente
                         * asignado al psicólogo autenticado.
                         */
                        .requestMatchers(
                                "/api/clinical-history/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "PSICOLOGO"
                        )

                        /*
                         * Cualquier ruta no declarada
                         * exige autenticación.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * Encabezados HTTP de seguridad.
                 */
                .headers(headers -> headers

                        /*
                         * Evita que el sistema se cargue dentro
                         * de un iframe externo.
                         */
                        .frameOptions(frame ->
                                frame.deny()
                        )

                        /*
                         * Evita que el navegador intente adivinar
                         * el tipo real de un archivo.
                         */
                        .contentTypeOptions(
                                Customizer.withDefaults()
                        )

                        /*
                         * Obliga al navegador a utilizar HTTPS
                         * después de visitar el sitio por HTTPS.
                         */
                        .httpStrictTransportSecurity(hsts ->
                                hsts
                                        .includeSubDomains(true)
                                        .preload(true)
                                        .maxAgeInSeconds(
                                                31_536_000
                                        )
                        )
                )

                /*
                 * Filtro JWT antes del filtro estándar
                 * de usuario y contraseña.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                /*
                 * Respuestas uniformes 401 y 403.
                 */
                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint(
                                (
                                        request,
                                        response,
                                        exception
                                ) -> {
                                    auditUnauthorizedRequest(
                                            request
                                    );

                                    writeJsonError(
                                            response,
                                            HttpServletResponse
                                                    .SC_UNAUTHORIZED,
                                            "No autorizado"
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (
                                        request,
                                        response,
                                        exception
                                ) -> {
                                    auditAccessDenied(
                                            request
                                    );

                                    writeJsonError(
                                            response,
                                            HttpServletResponse
                                                    .SC_FORBIDDEN,
                                            "Acceso denegado"
                                    );
                                }
                        )
                );

        return http.build();
    }

    /*
     * =========================================================
     * CORS
     * =========================================================
     */

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        List<String> origins =
                Arrays.stream(
                                allowedOrigins.split(",")
                        )
                        .map(String::trim)
                        .filter(origin ->
                                !origin.isBlank()
                        )
                        .distinct()
                        .toList();

        configuration.setAllowedOrigins(
                origins
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS",
                        "HEAD"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        /*
         * Actualmente el JWT viaja en Authorization,
         * no dentro de una cookie.
         */
        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    /*
     * =========================================================
     * AUTENTICACIÓN
     * =========================================================
     */

    @Bean
    public UserDetailsService userDetailsService() {

        return email -> {

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Usuario no encontrado"
                                    )
                            );

            if (
                    user.getRole() == null ||
                    user.getRole().isBlank()
            ) {
                throw new UsernameNotFoundException(
                        "Usuario sin rol asignado"
                );
            }

            String authority =
                    normalizeAuthority(
                            user.getRole()
                    );

            return new org.springframework.security
                    .core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Boolean.TRUE.equals(
                            user.getActive()
                    ),
                    true,
                    true,
                    true,
                    List.of(
                            new SimpleGrantedAuthority(
                                    authority
                            )
                    )
            );
        };
    }

    @Bean
    public DaoAuthenticationProvider
    authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService()
        );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        /*
         * Los hashes existentes de BCrypt seguirán funcionando.
         * Las contraseñas nuevas utilizarán factor 12.
         */
        return new BCryptPasswordEncoder(12);
    }

    /*
     * =========================================================
     * RESPUESTAS DE ERROR
     * =========================================================
     */

    private void writeJsonError(
            HttpServletResponse response,
            int status,
            String message
    ) throws java.io.IOException {

        response.setStatus(status);

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                """
                {
                    "error": "%s",
                    "message": "%s"
                }
                """.formatted(
                        message,
                        message
                )
        );
    }

    /*
     * =========================================================
     * AUDITORÍA DE SEGURIDAD
     * =========================================================
     */

    private void auditUnauthorizedRequest(
            HttpServletRequest request
    ) {

        String uri =
                sanitizeLogValue(
                        request.getRequestURI(),
                        300
                );

        if (
                uri == null ||
                !uri.startsWith("/api/")
        ) {
            return;
        }

        if (
                shouldSkipUnauthorizedAudit(uri)
        ) {
            return;
        }

        auditLogService.recordSecurity(
                "SEGURIDAD",
                "NO AUTORIZADO",
                "Endpoint",
                null,
                "Intento de acceso sin autenticación válida a "
                        + sanitizeLogValue(
                                request.getMethod(),
                                15
                        )
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

    private void auditAccessDenied(
            HttpServletRequest request
    ) {

        Authentication authentication =
                org.springframework.security.core
                        .context.SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getName() != null
                        ? sanitizeLogValue(
                                authentication.getName(),
                                150
                        )
                        : "desconocido";

        String role =
                authentication != null &&
                authentication.getAuthorities() != null
                        ? authentication
                                .getAuthorities()
                                .stream()
                                .findFirst()
                                .map(authority ->
                                        normalizeAuthority(
                                                authority.getAuthority()
                                        )
                                )
                                .orElse("SIN_ROL")
                        : "SIN_ROL";

        String uri =
                sanitizeLogValue(
                        request.getRequestURI(),
                        300
                );

        boolean sensitive =
                isSensitiveEndpoint(uri);

        auditLogService.recordSecurity(
                "SEGURIDAD",
                sensitive
                        ? "ACCESO DENEGADO CRÍTICO"
                        : "ACCESO DENEGADO",
                "Endpoint",
                null,
                "El usuario intentó acceder sin permisos a "
                        + sanitizeLogValue(
                                request.getMethod(),
                                15
                        )
                        + " "
                        + uri
                        + ". "
                        + getClientInfo(request),
                email,
                role,
                sensitive
                        ? "CRITICAL"
                        : "WARNING",
                sensitive
        );
    }

    /*
     * Evita registrar constantemente la validación normal
     * de una sesión que ya expiró.
     */
    private boolean shouldSkipUnauthorizedAudit(
            String uri
    ) {
        return uri.startsWith(
                "/api/auth/validate"
        );
    }

    private boolean isSensitiveEndpoint(
            String uri
    ) {

        if (uri == null) {
            return false;
        }

        return uri.startsWith("/api/users")
                || uri.startsWith(
                        "/api/admin-notifications"
                )
                || uri.startsWith(
                        "/api/promotions"
                )
                || uri.startsWith(
                        "/api/finances"
                )
                || uri.startsWith(
                        "/api/reports"
                )
                || uri.startsWith(
                        "/api/audit-logs"
                )
                || uri.startsWith(
                        "/api/profile"
                )
                || uri.startsWith(
                        "/api/services"
                )
                || uri.startsWith(
                        "/api/psychologists"
                )
                || uri.startsWith(
                        "/api/patients"
                )
                || uri.startsWith(
                        "/api/leads"
                )
                || uri.startsWith(
                        "/api/appointments"
                )
                || uri.startsWith(
                        "/api/psychologist-availabilities"
                )
                || uri.startsWith(
                        "/api/clinical-history"
                );
    }

    private String getClientInfo(
            HttpServletRequest request
    ) {

        String ip =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (
                ip == null ||
                ip.isBlank()
        ) {
            ip = request.getRemoteAddr();
        }

        if (
                ip != null &&
                ip.contains(",")
        ) {
            ip = ip
                    .split(",")[0]
                    .trim();
        }

        ip = sanitizeLogValue(
                ip,
                64
        );

        if (
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "::1".equals(ip)
        ) {
            ip = "localhost";
        }

        String userAgent =
                request.getHeader(
                        "User-Agent"
                );

        if (
                userAgent == null ||
                userAgent.isBlank()
        ) {
            userAgent =
                    "No identificado";
        }

        userAgent =
                sanitizeLogValue(
                        userAgent,
                        300
                );

        userAgent =
                simplifyUserAgent(
                        userAgent
                );

        return "IP: "
                + ip
                + ". Navegador: "
                + userAgent;
    }

    /*
     * Evita saltos de línea y valores enormes
     * dentro de los registros de auditoría.
     */
    private String sanitizeLogValue(
            String value,
            int maximumLength
    ) {

        if (value == null) {
            return "";
        }

        String sanitized =
                value
                        .replace("\r", "")
                        .replace("\n", "")
                        .trim();

        if (
                sanitized.length() >
                maximumLength
        ) {
            return sanitized.substring(
                    0,
                    maximumLength
            );
        }

        return sanitized;
    }

    private String simplifyUserAgent(
            String userAgent
    ) {

        String browser =
                "Navegador desconocido";

        String os =
                "Sistema desconocido";

        if (
                userAgent.contains(
                        "Windows NT 10.0"
                )
        ) {
            os = "Windows 10/11";

        } else if (
                userAgent.contains("Windows")
        ) {
            os = "Windows";

        } else if (
                userAgent.contains("Android")
        ) {
            os = "Android";

        } else if (
                userAgent.contains("iPhone") ||
                userAgent.contains("iPad")
        ) {
            os = "iOS";

        } else if (
                userAgent.contains("Mac OS")
        ) {
            os = "macOS";

        } else if (
                userAgent.contains("Linux")
        ) {
            os = "Linux";
        }

        if (
                userAgent.contains("Edg/")
        ) {
            browser = "Microsoft Edge";

        } else if (
                userAgent.contains("Chrome/")
        ) {
            browser = "Google Chrome";

        } else if (
                userAgent.contains("Firefox/")
        ) {
            browser = "Mozilla Firefox";

        } else if (
                userAgent.contains("Safari/")
        ) {
            browser = "Safari";
        }

        return browser + " en " + os;
    }

    private String normalizeAuthority(
            String role
    ) {

        if (
                role == null ||
                role.isBlank()
        ) {
            return "SIN_ROL";
        }

        String normalized =
                role
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                normalized.startsWith(
                        "ROLE_"
                )
        ) {
            normalized =
                    normalized.substring(5);
        }

        return normalized;
    }
}