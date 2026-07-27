package com.jahm.bancocapsula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistroController {
@GetMapping("/registro")
    public String mostrarRegistro() {
        // Retorna el nombre exacto de tu archivo HTML sin la extensión .html
        return "registro"; 
    }
}
