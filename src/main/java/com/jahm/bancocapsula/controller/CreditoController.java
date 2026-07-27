package com.jahm.bancocapsula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.SolicitudCreditoRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CreditoController {

    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final UsuarioRepository usuarioRepository;

    public CreditoController(SolicitudCreditoRepository solicitudCreditoRepository,
                           UsuarioRepository usuarioRepository) {
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ============================================================
    // SOLICITAR CRÉDITO - Mostrar formulario
    // ============================================================
    @GetMapping("/credito")
    public String mostrarFormularioCredito(Model modelo, Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        return usuarioRepository.findByUsername(auth.getName())
                .map(usuario -> {
                    List<SolicitudCreditoEntity> solicitudes = solicitudCreditoRepository.findByUsuarioOrderByFechaDesc(usuario);
                    modelo.addAttribute("solicitudes", solicitudes);
                    modelo.addAttribute("usuario", usuario);
                    return "credito";
                })
                .orElse("redirect:/login");
    }

    // ============================================================
    // PROCESAR SOLICITUD DE CRÉDITO - POST
    // ============================================================
    @PostMapping("/credito/solicitar")
    public String procesarCredito(
            @RequestParam Double monto,
            @RequestParam String firmaData,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        
        if (auth == null) {
            return "redirect:/login";
        }

        String username = auth.getName();

        // Validaciones
        if (monto == null || monto <= 0) {
            redirectAttributes.addFlashAttribute("error", "El monto debe ser mayor a 0");
            return "redirect:/credito";
        }
        if (firmaData == null || firmaData.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La firma es obligatoria");
            return "redirect:/credito";
        }
        
        try {
            UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            SolicitudCreditoEntity solicitud = new SolicitudCreditoEntity();
            solicitud.setUsuario(usuario);
            solicitud.setMontoSolicitado(monto);
            solicitud.setEstado("PENDIENTE");
            solicitud.setFecha(LocalDateTime.now());
            solicitud.setFirmaDigital(firmaData);
            
            solicitudCreditoRepository.save(solicitud);
            
            redirectAttributes.addFlashAttribute("success", "Solicitud enviada y pendiente de aprobación");
            return "redirect:/credito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar solicitud: " + e.getMessage());
            return "redirect:/credito";
        }
    }

    // ============================================================
    // MIS CRÉDITOS - Historial
    // ============================================================
    @GetMapping("/mis-creditos")
    public String misCreditos(Model modelo, Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        return usuarioRepository.findByUsername(auth.getName())
                .map(usuario -> {
                    List<SolicitudCreditoEntity> creditos = solicitudCreditoRepository.findByUsuarioOrderByFechaDesc(usuario);
                    modelo.addAttribute("creditos", creditos);
                    modelo.addAttribute("usuario", usuario);
                    modelo.addAttribute("rol", usuario.getRol());
                    return "mis-creditos";
                })
                .orElse("redirect:/login");
    }

    // ============================================================
    // REDIRECCIONAR
    // ============================================================
    @GetMapping("/creditos")
    public String redirigir() {
        return "redirect:/credito";
    }
}