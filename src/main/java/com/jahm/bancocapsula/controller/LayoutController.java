package com.jahm.bancocapsula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController {

    @GetMapping("/layout")
    public String mostrarLayout() {
        // Retorna el nombre exacto de tu archivo HTML sin la extensión .html
        return "layout"; 
    }
}
