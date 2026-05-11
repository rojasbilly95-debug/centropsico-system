package com.centropsicologico.sistema.config;

import com.centropsicologico.sistema.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    public SecurityConfig(
            UserRepository userRepository,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/portal.html",
                                "/css/**",
                                "/js/**",
                                "/components/**",
                                "/img/**",
                                "/favicon.ico",
                                "/api/auth/**",
                                "/api/public/**",
                                "/ws/**")
                        .permitAll()
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers("/api/users/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/leads/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/psychologists/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers(HttpMethod.GET, "/api/services/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers("/api/psychologists/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/services/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/finances/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/reports/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/patients/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA")

                        .requestMatchers(HttpMethod.GET, "/api/psychologist-availabilities/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers("/api/psychologist-availabilities/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/notifications/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers("/api/appointments/**")
                        .hasAnyAuthority("ADMIN", "RECEPCIONISTA", "PSICOLOGO")

                        .requestMatchers("/api/clinical-history/**")
                        .hasAnyAuthority("ADMIN", "PSICOLOGO")

                        .anyRequest().authenticated())

                .httpBasic(httpBasic -> httpBasic.disable())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"No autorizado\"}");
                        }));

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
                    List.of(new SimpleGrantedAuthority(user.getRole())));
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
}