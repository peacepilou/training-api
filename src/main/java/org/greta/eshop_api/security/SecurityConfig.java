package org.greta.eshop_api.security;

import org.greta.eshop_api.exceptions.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   CustomUserDetailsService userDetailsService,
                                                   JsonMapper jsonMapper) throws Exception {
        // Filtres créés ici avec new, pas déclarés en @Component : un filtre bean serait
        // aussi enregistré par Spring Boot dans le serveur, en plus de la chaîne de sécurité
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        JwtExceptionHandlerFilter jwtExceptionHandlerFilter = new JwtExceptionHandlerFilter(jsonMapper);

        http
                // Le jeton voyage dans l'en-tête Authorization, que le navigateur n'ajoute jamais tout seul :
                // un site tiers ne peut pas le joindre à une requête forgée. Avec un cookie de session, il faudrait garder le CSRF
                .csrf(csrf -> csrf.disable())
                // Sans bean CorsConfigurationSource, Spring Security reprend la config CORS de WebMvcConfig (chapitre 08)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Spring prend la première règle qui correspond : la plus précise d'abord
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/candles/**").permitAll()
                        .requestMatchers("/candles/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint(jsonMapper))
                        .accessDeniedHandler(jsonAccessDeniedHandler(jsonMapper)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionHandlerFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    // Appelé quand une route protégée est demandée sans jeton : 401 en JSON au lieu d'un 403 vide
    private AuthenticationEntryPoint jsonAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            jsonMapper.writeValue(response.getWriter(), new ErrorResponse("Authentification requise."));
        };
    }

    // Appelé quand le jeton est valide mais le rôle insuffisant. Sans lui, Spring renvoie vers /error,
    // que le filtre JWT ne revoit pas : le client recevrait un 401 au lieu du 403
    private AccessDeniedHandler jsonAccessDeniedHandler(JsonMapper jsonMapper) {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            jsonMapper.writeValue(response.getWriter(), new ErrorResponse("Accès refusé pour ce rôle."));
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
