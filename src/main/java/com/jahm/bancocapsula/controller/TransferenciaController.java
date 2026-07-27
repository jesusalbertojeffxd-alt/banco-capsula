package com.jahm.bancocapsula.controller;

import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.CuentaRepository;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import com.jahm.bancocapsula.service.TransferenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TransferenciaController {

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MovimientoCuentaRepository movimientoCuentaRepository;

    // ============================================================
    // TRANSFERENCIAS
    // ============================================================

    // ✅ Mis Transferencias (historial)
    @GetMapping("/transferencia")
    public String mostrarTransferencias(Authentication auth, Model model) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<CuentaEntity> cuentasUsuario = cuentaRepository.findByUsuario(usuario);
        CuentaEntity cuentaPrincipal = cuentasUsuario.isEmpty() ? null : cuentasUsuario.get(0);
        
        Double saldoTotal = cuentaPrincipal != null ? cuentaPrincipal.getSaldo() : 0.0;
        String cuentaClabe = cuentaPrincipal != null ? cuentaPrincipal.getClabe() : "Sin cuenta";
        
        List<MovimientoEntity> movimientos = null;
        if (cuentaPrincipal != null) {
            movimientos = movimientoCuentaRepository.findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(
                cuentaClabe, cuentaClabe);
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("cuentaClabe", cuentaClabe);
        model.addAttribute("movimientos", movimientos != null ? movimientos : List.of());
        model.addAttribute("rol", usuario.getRol());
        
        return "transferencia";
    }

    // ✅ Nueva Transferencia (formulario)
    @GetMapping("/transferencia/nueva")
    public String nuevaTransferencia(Authentication auth, Model model) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<CuentaEntity> cuentasUsuario = cuentaRepository.findByUsuario(usuario);
        CuentaEntity cuentaPrincipal = cuentasUsuario.isEmpty() ? null : cuentasUsuario.get(0);
        
        Double saldoTotal = cuentaPrincipal != null ? cuentaPrincipal.getSaldo() : 0.0;
        String cuentaClabe = cuentaPrincipal != null ? cuentaPrincipal.getClabe() : "Sin cuenta";
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("cuentaClabe", cuentaClabe);
        model.addAttribute("rol", usuario.getRol());
        
        return "transferencia-nueva";
    }

    // ✅ Realizar Transferencia
    @PostMapping("/transferencia/realizar")
    public String realizarTransferencia(@RequestParam String cuentaOrigen,
                                        @RequestParam String cuentaDestino,
                                        @RequestParam Double monto,
                                        @RequestParam(required = false) String descripcion,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {
        try {
            transferenciaService.realizarTransferencia(cuentaOrigen, cuentaDestino, monto, descripcion);
            redirectAttributes.addFlashAttribute("exito", "Transferencia realizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al realizar transferencia: " + e.getMessage());
        }
        return "redirect:/transferencia";
    }

    // ✅ Mis Transferencias (alias)
    @GetMapping("/mis-transferencias")
    public String verMisTransferencias(Authentication auth, Model model) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<CuentaEntity> cuentas = cuentaRepository.findByUsuario(usuario);
        List<String> cuentasClabes = cuentas.stream()
            .map(CuentaEntity::getClabe)
            .collect(Collectors.toList());
        
        List<MovimientoEntity> transferencias = null;
        if (!cuentasClabes.isEmpty()) {
            transferencias = movimientoCuentaRepository
                .findByCuentaOrigenInOrCuentaDestinoInOrderByFechaDesc(cuentasClabes, cuentasClabes);
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("transferencias", transferencias != null ? transferencias : List.of());
        model.addAttribute("rol", usuario.getRol());
        
        return "mis-transferencias";
    }
}