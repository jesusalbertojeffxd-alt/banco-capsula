package com.jahm.bancocapsula.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.UsuarioRepository;
//LA UNICA FUNCION ES CREAR UN USUSARIO EJECUTIVO Y UN USUSARIO CLIENTE
@Component

public class DataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuario, PasswordEncoder pass) {
        this.usuarioRepository = usuario;
        this.passwordEncoder = pass;
    }

    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.count() == 0) {
            // Crear un usuario ejecutivo
            System.out.println("Agregando Prueba usuario Ejecutivo...");
            // 1 Creando un usuario ejecutivo
            UsuarioEntity ejecutivo = new UsuarioEntity();
            ejecutivo.setUsername("Alberto");
            ejecutivo.setNombre("Jesus Alberto Hernandez Molina");
            ejecutivo.setPassword(passwordEncoder.encode("1234"));
            ejecutivo.setRol("EJECUTIVO");
            usuarioRepository.save(ejecutivo);
            System.out.println("Usuario Ejecutivo agregado: JAHM-1234");

            // 2 Creando un usuario cliente
            UsuarioEntity cliente = new UsuarioEntity();
            cliente.setUsername("Lesly");
            cliente.setNombre("Lesly Joana Alonzo Morales");
            cliente.setPassword(passwordEncoder.encode("12345"));
            cliente.setRol("CLIENTE");
            usuarioRepository.save(cliente);
            System.out.println("Usuario Cliente agregado: Lesly-12345");

        }

    }
}