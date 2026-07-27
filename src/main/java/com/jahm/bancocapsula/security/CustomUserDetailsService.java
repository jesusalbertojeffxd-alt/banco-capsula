package com.jahm.bancocapsula.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuario) {
        this.usuarioRepository= usuario;
    }

    @Override
    public UserDetails loadUserByUsername(String username)  {
       UsuarioEntity usuariologin = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado: " + username));
        //aqui se usa el buildder de spring security para crear un objeto UserDetails a partir de la entidad UsuarioEntity
        //se agrega 
        
                String rol = usuariologin.getRol();
                if (rol == null || rol.isBlank()) {
                    rol = "CLIENTE";
                }
                rol = rol.trim().toUpperCase(Locale.ROOT).replaceFirst("^ROLE_", "");

                return User.builder()
                .username(usuariologin.getUsername())
                .password(usuariologin.getPassword())
                .roles(rol)
                .build();
    }
    // Implementación de la lógica para cargar los detalles del usuario desde la base de datos

}
