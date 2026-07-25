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

import org.springframework.security.authentication.dao
        .DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.method.configuration
        .EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders
        .HttpSecurity;

import org.springframework.security.config.annotation.web.configuration
        .EnableWebSecurity;

import org.springframework.security.config.http
        .SessionCreationPolicy;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import org.springframework.security.core.userdetails
        .UserDetailsService;

import org.springframework.security.core.userdetails
        .UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt
        .BCryptPasswordEncoder;

import org.springframework.security.crypto.password
        .PasswordEncoder;

import org.springframework.security.web
        .SecurityFilterChain;

import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors
        .CorsConfiguration;

import org.springframework.web.cors
        .CorsConfigurationSource;

import org.springframework.web.cors
        .UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final AuditLogService auditLogService;

    /*
     * Puede contener uno o varios dominios
     * separados por coma.
     *
     * Ejemplo:
     *
     * APP_ALLOWED_ORIGINS=
     * http://localhost:8080,
     * https://centropsico-system.onrender.com
     */
    @Value(
            "${app.security.allowed-origins:"
                    + "http://localhost:8080,"
                    + "https://centropsico-system.onrender.com}"
    )
    private String allowedOrigins;

    public SecurityConfig(
            UserRepository userRepository,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuditLogService auditLogService
    ) {
        this.userRepository =
                userRepository;

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.auditLogService =
                auditLogService;
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
                 * Actualmente el JWT se envía mediante:
                 *
                 * Authorization: Bearer <token>
                 *
                 * Mientras el token no se almacene en una
                 * cookie de autenticación, CSRF permanece
                 * deshabilitado.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                /*
                 * Configuración CORS centralizada.
                 */
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                /*
                 * El backend no mantiene sesiones HTTP.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Se desactivan mecanismos no utilizados
                 * porque la autenticación se realiza con JWT.
                 */
                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(basic ->
                        basic.disable()
                )

                .logout(logout ->
                        logout.disable()
                )

                .rememberMe(rememberMe ->
                        rememberMe.disable()
                )

                .requestCache(cache ->
                        cache.disable()
                )

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
                         * Solicitudes previas de CORS.
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * =========================================
                         * PÁGINAS Y RECURSOS PÚBLICOS
                         * =========================================
                         *
                         * Las páginas de recuperación deben
                         * abrirse sin un token JWT.
                         */
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/portal.html",
                                "/forgot-password.html",
                                "/reset-password.html",
                                "/error",
                                "/favicon.ico",
                                "/robots.txt",
                                "/css/**",
                                "/js/**",
                                "/components/**",
                                "/img/**"
                        )
                        .permitAll()

                        /*
                         * Las imágenes existentes continúan
                         * siendo públicas para no romper las
                         * etiquetas <img src="...">.
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
                         * =========================================
                         * AUTENTICACIÓN PÚBLICA
                         * =========================================
                         */

                        /*
                         * Inicio de sesión.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        )
                        .permitAll()

                        /*
                         * Comprobación del JWT almacenado
                         * en el navegador.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/validate"
                        )
                        .permitAll()

                        /*
                         * Solicitar recuperación.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/forgot-password"
                        )
                        .permitAll()

                        /*
                         * Validar enlace de recuperación.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/reset-password/validate"
                        )
                        .permitAll()

                        /*
                         * Guardar la contraseña nueva.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/reset-password"
                        )
                        .permitAll()

                        /*
                         * Cerrar sesión requiere que el token
                         * actual continúe siendo válido.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/logout"
                        )
                        .authenticated()

                        /*
                         * Portal público.
                         */
                        .requestMatchers(
                                "/api/public/**"
                        )
                        .permitAll()

                        /*
                         * El handshake de SockJS continúa
                         * siendo público.
                         *
                         * La autenticación de las suscripciones
                         * STOMP debe realizarse en WebSocketConfig.
                         */
                        .requestMatchers(
                                "/ws/**"
                        )
                        .permitAll()

                        /*
                         * =========================================
                         * PERFIL
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/profile/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * =========================================
                         * DASHBOARD
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/dashboard/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * =========================================
                         * AUDITORÍA
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/audit-logs/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * USUARIOS
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/users/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * NOTIFICACIONES ADMINISTRATIVAS
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/admin-notifications/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * PROMOCIONES
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/promotions/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * PRE-RESERVAS
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/leads/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * =========================================
                         * PSICÓLOGOS: CONSULTA
                         * =========================================
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
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * SERVICIOS: CONSULTA
                         * =========================================
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
                        .requestMatchers(
                                "/api/services/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * FINANZAS
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/finances/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * REPORTES
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/reports/**"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * PACIENTES
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/patients/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * =========================================
                         * DISPONIBILIDAD: CONSULTA
                         * =========================================
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
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * NOTIFICACIONES
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/notifications/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA",
                                "PSICOLOGO"
                        )

                        /*
                         * =========================================
                         * CITAS DEL PSICÓLOGO
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
                         * Consultar psicólogos relacionados
                         * con un servicio.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/"
                                        + "psychologists-by-service"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "RECEPCIONISTA"
                        )

                        /*
                         * =========================================
                         * CONSULTA GENERAL DE CITAS
                         * =========================================
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
                         * =========================================
                         * CREAR CITA
                         * =========================================
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
                         * =========================================
                         * REGISTRAR PAGO
                         * =========================================
                         *
                         * La regla específica debe declararse
                         * antes de la actualización general.
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
                         * =========================================
                         * CAMBIAR ESTADO
                         * =========================================
                         *
                         * El servicio comprueba que un psicólogo
                         * solo modifique sus propias citas.
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
                         * =========================================
                         * ACTUALIZACIÓN GENERAL
                         * =========================================
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
                         * =========================================
                         * ELIMINAR CITA
                         * =========================================
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/appointments/*"
                        )
                        .hasAuthority(
                                "ADMIN"
                        )

                        /*
                         * =========================================
                         * HISTORIA CLÍNICA
                         * =========================================
                         *
                         * Además del rol, el servicio debe comprobar
                         * que el paciente esté relacionado con una
                         * cita del psicólogo autenticado.
                         */
                        .requestMatchers(
                                "/api/clinical-history/**"
                        )
                        .hasAnyAuthority(
                                "ADMIN",
                                "PSICOLOGO"
                        )

                        /*
                         * Cualquier ruta no declarada requiere
                         * un usuario autenticado.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * =================================================
                 * ENCABEZADOS HTTP DE SEGURIDAD
                 * =================================================
                 */
                .headers(headers -> headers

                        /*
                         * Evita que CentroPsico sea cargado
                         * dentro de un iframe externo.
                         */
                        .frameOptions(frameOptions ->
                                frameOptions.deny()
                        )

                        /*
                         * Evita que el navegador intente
                         * adivinar tipos de contenido.
                         */
                        .contentTypeOptions(
                                Customizer.withDefaults()
                        )

                        /*
                         * Obliga al navegador a utilizar HTTPS
                         * después de acceder mediante HTTPS.
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
                 * El filtro JWT se ejecuta antes del filtro
                 * estándar de usuario y contraseña.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                /*
                 * =================================================
                 * RESPUESTAS 401 Y 403
                 * =================================================
                 */
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling

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
         * El JWT viaja en el encabezado Authorization,
         * no dentro de una cookie.
         */
        configuration.setAllowCredentials(
                false
        );

        configuration.setMaxAge(
                3600L
        );

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
                    userRepository
                            .findByEmailIgnoreCase(email)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Usuario no encontrado"
                                    )
                            );

            if (
                    user.getRole() == null
                    || user.getRole().isBlank()
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
         * Los hashes anteriores de BCrypt continúan
         * funcionando.
         *
         * Las contraseñas nuevas utilizan factor 12.
         */
        return new BCryptPasswordEncoder(
                12
        );
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

        response.setStatus(
                status
        );

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
                uri.isBlank()
                || !uri.startsWith("/api/")
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
                authentication != null
                        && authentication.isAuthenticated()
                        && authentication.getName() != null

                        ? sanitizeLogValue(
                                authentication.getName(),
                                150
                        )

                        : "desconocido";

        String role =
                authentication != null
                        && authentication.getAuthorities() != null

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
     * Evita registrar repetidamente solicitudes normales
     * asociadas con la validación de una sesión expirada.
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

        if (
                uri == null
                || uri.isBlank()
        ) {
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
                ip == null
                || ip.isBlank()
        ) {
            ip =
                    request.getRemoteAddr();
        }

        if (
                ip != null
                && ip.contains(",")
        ) {
            ip =
                    ip.split(",")[0]
                            .trim();
        }

        ip =
                sanitizeLogValue(
                        ip,
                        64
                );

        if (
                "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)
        ) {
            ip =
                    "localhost";
        }

        String userAgent =
                request.getHeader(
                        "User-Agent"
                );

        if (
                userAgent == null
                || userAgent.isBlank()
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
     * Evita saltos de línea y valores demasiado grandes
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
                sanitized.length()
                        > maximumLength
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

        String operatingSystem =
                "Sistema desconocido";

        if (
                userAgent.contains(
                        "Windows NT 10.0"
                )
        ) {
            operatingSystem =
                    "Windows 10/11";

        } else if (
                userAgent.contains(
                        "Windows"
                )
        ) {
            operatingSystem =
                    "Windows";

        } else if (
                userAgent.contains(
                        "Android"
                )
        ) {
            operatingSystem =
                    "Android";

        } else if (
                userAgent.contains("iPhone")
                || userAgent.contains("iPad")
        ) {
            operatingSystem =
                    "iOS";

        } else if (
                userAgent.contains(
                        "Mac OS"
                )
        ) {
            operatingSystem =
                    "macOS";

        } else if (
                userAgent.contains(
                        "Linux"
                )
        ) {
            operatingSystem =
                    "Linux";
        }

        if (
                userAgent.contains(
                        "Edg/"
                )
        ) {
            browser =
                    "Microsoft Edge";

        } else if (
                userAgent.contains(
                        "Chrome/"
                )
        ) {
            browser =
                    "Google Chrome";

        } else if (
                userAgent.contains(
                        "Firefox/"
                )
        ) {
            browser =
                    "Mozilla Firefox";

        } else if (
                userAgent.contains(
                        "Safari/"
                )
        ) {
            browser =
                    "Safari";
        }

        return browser
                + " en "
                + operatingSystem;
    }

    private String normalizeAuthority(
            String role
    ) {

        if (
                role == null
                || role.isBlank()
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