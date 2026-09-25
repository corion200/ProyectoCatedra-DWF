package sv.edu.udb.cfc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SEGURIDAD DEFINITIVA (Módulo 9 / RF11, RF12): JWT stateless + control de
 * acceso por los 4 roles del enunciado (ADMIN, RECEPCIONISTA, CLIENTE, CONTABILIDAD).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Bean que encripta y valida contraseñas con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Orquestador de autenticación que usa AuthService.login(). */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos y documentación
                        .requestMatchers("/", "/index.html").permitAll()
                        .requestMatchers(org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // 🟢 Catálogos y Cursos: Lectura pública (GET) para clientes y visitantes
                        .requestMatchers(HttpMethod.GET, "/api/v1/cursos/**", "/api/v1/categorias/**",
                                "/api/v1/modalidades/**", "/api/v1/docentes/**",
                                "/api/v1/espacios/**").permitAll()

                        // 🔴 Catálogos: Solo ADMIN puede escribir (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/v1/cursos/**", "/api/v1/categorias/**",
                                "/api/v1/modalidades/**", "/api/v1/docentes/**",
                                "/api/v1/espacios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cursos/**", "/api/v1/categorias/**",
                                "/api/v1/modalidades/**", "/api/v1/docentes/**",
                                "/api/v1/espacios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cursos/**", "/api/v1/categorias/**",
                                "/api/v1/modalidades/**", "/api/v1/docentes/**",
                                "/api/v1/espacios/**").hasRole("ADMIN")

                        // Operación diaria: Recepción y Admin
                        .requestMatchers("/api/v1/inscripciones/**", "/api/v1/clientes/**",
                                "/api/v1/cotizaciones/**", "/api/v1/alquileres/**",
                                "/api/v1/caterings/**", "/api/v1/agenda/**").hasAnyRole("ADMIN", "RECEPCIONISTA")

                        .requestMatchers("/api/v1/auth/cambiar-password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")

                        // Financiero: Contabilidad y Admin
                        .requestMatchers("/api/v1/pagos/**").hasAnyRole("ADMIN", "CONTABILIDAD")

                        // Cualquier otra petición requiere autenticación por defecto
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}