package com.jahm.bancocapsula.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.CuentaRepository;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;
    private final CuentaRepository cuentaRepository;

    public DashboardController(UsuarioRepository usuario, 
                               MovimientoCuentaRepository movi,
                               CuentaRepository cuentaRepository) {
        this.usuarioRepository = usuario;
        this.movimientoCuentaRepository = movi;
        this.cuentaRepository = cuentaRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model modelo, Authentication auth) {
        if(auth == null) {
            return "redirect:/login";
        }

        String username = auth.getName();
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if("EJECUTIVO".equals(usuario.getRol()) || "ADMIN".equals(usuario.getRol())) {
            return "redirect:/admin/dashboard";
        }

        String clabe = "No tiene cuenta";
        Double saldo = 0.0;
        List<MovimientoEntity> todosLosMovimientos = new ArrayList<>(); 
        List<MovimientoEntity> ultimosMovimientos = new ArrayList<>();
        String numeroTarjeta = "**** **** **** 0000";
        
        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
            CuentaEntity cuentaPrincipal = usuario.getCuentas().get(0); 
            clabe = cuentaPrincipal.getClabe();
            saldo = cuentaPrincipal.getSaldo();
            
            if (clabe != null && clabe.length() >= 16) {
                numeroTarjeta = clabe.substring(0, 4) + " " + 
                               clabe.substring(4, 8) + " " + 
                               clabe.substring(8, 12) + " " + 
                               clabe.substring(12, 16);
            }
            
            todosLosMovimientos = movimientoCuentaRepository
                    .findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(clabe, clabe);
            
            int maxSize = Math.min(todosLosMovimientos.size(), 10);
            for (int i = 0; i < maxSize; i++) {
                ultimosMovimientos.add(todosLosMovimientos.get(i));
            }
        }

        modelo.addAttribute("nombreCliente", usuario.getNombre());
        modelo.addAttribute("saldoTotal", saldo);
        modelo.addAttribute("cuentaClabe", clabe);
        modelo.addAttribute("movimientos", ultimosMovimientos);
        modelo.addAttribute("rol", usuario.getRol());
        modelo.addAttribute("numeroTarjeta", numeroTarjeta);

        // Datos para gráfica de pastel
        Map<String, Double> gastosPorTipo = new HashMap<>();
        Map<String, String> coloresPorTipo = new HashMap<>();
        coloresPorTipo.put("EGRESO", "#ff1744");
        coloresPorTipo.put("INGRESO", "#00e676");
        coloresPorTipo.put("Pago", "#ff1744");
        coloresPorTipo.put("Compra", "#ff9100");
        coloresPorTipo.put("Transferencia", "#2979ff");
        coloresPorTipo.put("Depósito", "#00e676");
        coloresPorTipo.put("Retiro", "#ff6d00");
        coloresPorTipo.put("Otro", "#9e9e9e");
        
        for (MovimientoEntity m : ultimosMovimientos) {
            if (clabe.equals(m.getCuentaOrigen())) {
                String tipo = m.getTipo();
                if (tipo == null || tipo.isEmpty()) {
                    tipo = "Otro";
                }
                Double montoActual = gastosPorTipo.getOrDefault(tipo, 0.0);
                gastosPorTipo.put(tipo, montoActual + m.getMonto());
            }
        }
        
        List<String> etiquetasPastel = new ArrayList<>();
        List<Double> valoresPastel = new ArrayList<>();
        List<String> coloresPastel = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : gastosPorTipo.entrySet()) {
            etiquetasPastel.add(entry.getKey());
            valoresPastel.add(entry.getValue());
            String color = coloresPorTipo.getOrDefault(entry.getKey(), "#9e9e9e");
            coloresPastel.add(color);
        }
        
        if (gastosPorTipo.isEmpty()) {
            etiquetasPastel.add("Sin gastos");
            valoresPastel.add(1.0);
            coloresPastel.add("#6c757d");
        }
        
        modelo.addAttribute("etiquetasPastel", etiquetasPastel);
        modelo.addAttribute("valoresPastel", valoresPastel);
        modelo.addAttribute("coloresPastel", coloresPastel);

        return "dashboard";
    }

    // ✅ NUEVO: DEPOSITAR
    @PostMapping("/dashboard/depositar")
    public String depositar(@RequestParam Double monto,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            CuentaEntity cuenta = cuentaRepository.findByUsuario(usuario).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            
            cuenta.setSaldo(cuenta.getSaldo() + monto);
            cuentaRepository.save(cuenta);
            
            MovimientoEntity movimiento = new MovimientoEntity();
            movimiento.setCuentaOrigen("DEPOSITO_EXTERNO");
            movimiento.setCuentaDestino(cuenta.getClabe());
            movimiento.setMonto(monto);
            movimiento.setTipo("DEPOSITO");
            movimiento.setDescripcion("Depósito en efectivo");
            movimiento.setFecha(java.time.LocalDate.now());
            movimiento.setEstadoMovimiento("AUTORIZADO");
            movimientoCuentaRepository.save(movimiento);
            
            redirectAttributes.addFlashAttribute("success", "Depósito realizado: $" + monto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al depositar: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ✅ NUEVO: RETIRAR
    @PostMapping("/dashboard/retirar")
    public String retirar(@RequestParam Double monto,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            CuentaEntity cuenta = cuentaRepository.findByUsuario(usuario).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            
            if (cuenta.getSaldo() < monto) {
                throw new RuntimeException("Saldo insuficiente");
            }
            
            cuenta.setSaldo(cuenta.getSaldo() - monto);
            cuentaRepository.save(cuenta);
            
            MovimientoEntity movimiento = new MovimientoEntity();
            movimiento.setCuentaOrigen(cuenta.getClabe());
            movimiento.setCuentaDestino("RETIRO_EXTERNO");
            movimiento.setMonto(monto);
            movimiento.setTipo("RETIRO");
            movimiento.setDescripcion("Retiro de efectivo");
            movimiento.setFecha(java.time.LocalDate.now());
            movimiento.setEstadoMovimiento("AUTORIZADO");
            movimientoCuentaRepository.save(movimiento);
            
            redirectAttributes.addFlashAttribute("success", "Retiro realizado: $" + monto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al retirar: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}