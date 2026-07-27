package com.jahm.bancocapsula.controller;

import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        // Usamos findByUsername porque tu entidad usa username, no email
        UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/perfil/actualizar-imagen")
    public String actualizarImagen(@RequestParam("imagen") MultipartFile file, 
                                   Authentication auth, 
                                   RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Selecciona una imagen");
                return "redirect:/perfil";
            }
            
            UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            byte[] bytes = file.getBytes();
            String extension = getExtension(file.getOriginalFilename());
            String base64 = "data:image/" + extension + ";base64," 
                           + Base64.getEncoder().encodeToString(bytes);
            
            usuario.setFotoPerfil(base64);
            usuarioRepository.save(usuario);
            
            redirectAttributes.addFlashAttribute("success", "Imagen actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }
        return "redirect:/perfil";
    }

    private String getExtension(String filename) {
        if (filename == null) return "png";
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) return "png";
        return filename.substring(lastDot + 1).toLowerCase();
    }
}