package com.jahm.bancocapsula.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.SolicitudCreditoRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import com.jahm.bancocapsula.service.ReporteService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CreditoController {

    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private ReporteService reporteService;

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
    // PDF DEL CREDITO - VISTA PREVIA INDIVIDUAL
    // ============================================================
    @GetMapping("/credito/pdf/{id}")
    public ResponseEntity<ByteArrayResource> generarPdfCredito(@PathVariable Long id, Authentication auth) {
        try {
            SolicitudCreditoEntity credito = solicitudCreditoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credito no encontrado con ID: " + id));

            UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que el crédito pertenece al usuario autenticado o es ADMIN
            if (!credito.getUsuario().getId().equals(usuario.getId()) && !"ADMIN".equals(usuario.getRol())) {
                return ResponseEntity.status(403).build();
            }

            // Generar PDF con los datos del crédito
            List<SolicitudCreditoEntity> creditos = List.of(credito);
            ByteArrayOutputStream pdfStream = reporteService.generarReporteCreditos(usuario, creditos);
            ByteArrayResource resource = new ByteArrayResource(pdfStream.toByteArray());

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=credito_" + credito.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ============================================================
    // REDIRECCIONAR
    // ============================================================
    @GetMapping("/creditos")
    public String redirigir() {
        return "redirect:/credito";
    }
}
