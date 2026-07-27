package com.jahm.bancocapsula.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/img/**", "/styles.css").permitAll()
                .requestMatchers("/creditos/**").authenticated()
                .requestMatchers("/dashboard").authenticated()
                // ✅ CAMBIO: Usar hasAuthority en lugar de hasAnyRole
                .requestMatchers("/admin/**").hasAnyAuthority("EJECUTIVO", "ADMIN", "ROLE_EJECUTIVO", "ROLE_ADMIN")
                .requestMatchers("/perfil").authenticated()
                .requestMatchers("/perfil/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/logout", "/procesar-credito", "/perfil/actualizar-imagen")
            );

        return http.build();
    }
}