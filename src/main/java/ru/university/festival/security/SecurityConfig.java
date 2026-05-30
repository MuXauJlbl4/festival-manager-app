package ru.university.festival.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/styles/**", "/scripts/**", "/images/**").permitAll()
                        .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "ORGANIZER", "MANAGER")
                        .requestMatchers("/api/users/**", "/api/roles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/events/*/cancel").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/festivals/**", "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/venues/**", "/api/rooms/**", "/api/event-types/**").hasAnyRole("ADMIN", "ORGANIZER", "MANAGER", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/participants/**", "/api/event-participants/**", "/api/participant-types/**")
                        .hasAnyRole("ADMIN", "ORGANIZER", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/festivals/**", "/api/venues/**", "/api/rooms/**", "/api/event-types/**", "/api/participant-types/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/festivals/**", "/api/venues/**", "/api/rooms/**", "/api/event-types/**", "/api/participant-types/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/festivals/**", "/api/venues/**", "/api/rooms/**", "/api/event-types/**", "/api/participant-types/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/events/{id}/register").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/events/**", "/api/participants/**", "/api/event-participants/**")
                        .hasAnyRole("ADMIN", "ORGANIZER", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**", "/api/participants/**", "/api/event-participants/**")
                        .hasAnyRole("ADMIN", "ORGANIZER", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**", "/api/participants/**", "/api/event-participants/**")
                        .hasAnyRole("ADMIN", "ORGANIZER", "MANAGER")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
