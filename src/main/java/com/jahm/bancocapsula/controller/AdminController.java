package com.jahm.bancocapsula.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jahm.bancocapsula.dto.TransferenciaDTO;
import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.CuentaRepository;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.SolicitudCreditoRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import com.jahm.bancocapsula.service.BancaService;
import com.jahm.bancocapsula.service.TransferenciaService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BancaService bancaService;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;
    
    @Autowired
    private TransferenciaService transferenciaService;

    public AdminController(BancaService bancaService, 
                          UsuarioRepository usuarioRepository, 
                          SolicitudCreditoRepository solicitudCreditoRepository, 
                          CuentaRepository cuentaRepository,
                          MovimientoCuentaRepository movimientoCuentaRepository) {
        this.bancaService = bancaService;
        this.usuarioRepository = usuarioRepository;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoCuentaRepository = movimientoCuentaRepository;
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model modelo) {
        
        long totalClientes = usuarioRepository.countByRol("CLIENTE");
        modelo.addAttribute("totalClientes", totalClientes);
        
        long totalCuentas = cuentaRepository.count();
        modelo.addAttribute("totalCuentas", totalCuentas);
        
        long totalMovimientos = movimientoCuentaRepository.count();
        modelo.addAttribute("totalMovimientos", totalMovimientos);
        
        List<SolicitudCreditoEntity> todasSolicitudes = solicitudCreditoRepository.findAll();
        
        long creditosAutorizados = 0;
        long creditosActivos = 0;
        double carteraTotal = 0.0;
        
        for (SolicitudCreditoEntity s : todasSolicitudes) {
            if ("AUTORIZADO".equals(s.getEstado()) || "APROBADO".equals(s.getEstado())) {
                creditosAutorizados++;
            }
            if ("ACTIVO".equals(s.getEstado())) {
                creditosActivos++;
            }
            if (s.getMontoSolicitado() != null) {
                carteraTotal += s.getMontoSolicitado();
            }
        }
        
        modelo.addAttribute("creditosAutorizados", creditosAutorizados);
        modelo.addAttribute("creditosActivos", creditosActivos);
        modelo.addAttribute("totalCreditos", todasSolicitudes.size());
        modelo.addAttribute("carteraTotal", carteraTotal);
        
        Double saldoTotal = cuentaRepository.sumSaldo();
        if (saldoTotal == null) saldoTotal = 0.0;
        modelo.addAttribute("saldoTotal", saldoTotal);
        
        java.util.Map<String, Long> creditosPorEstado = new java.util.HashMap<>();
        for (SolicitudCreditoEntity s : todasSolicitudes) {
            String estado = s.getEstado();
            if (estado == null) estado = "SIN ESTADO";
            creditosPorEstado.put(estado, creditosPorEstado.getOrDefault(estado, 0L) + 1);
        }
        
        List<String> etiquetasCreditos = new java.util.ArrayList<>(creditosPorEstado.keySet());
        List<Long> valoresCreditos = new java.util.ArrayList<>(creditosPorEstado.values());
        
        List<String> coloresCreditos = etiquetasCreditos.stream()
                .map(estado -> {
                    switch (estado.toUpperCase()) {
                        case "AUTORIZADO":
                        case "APROBADO":
                            return "#00ff88";
                        case "PENDIENTE":
                            return "#ffc107";
                        case "RECHAZADO":
                            return "#ff1744";
                        case "ACTIVO":
                            return "#00bfff";
                        default:
                            return "#6c757d";
                    }
                })
                .collect(Collectors.toList());
        
        modelo.addAttribute("etiquetasCreditos", etiquetasCreditos);
        modelo.addAttribute("valoresCreditos", valoresCreditos);
        modelo.addAttribute("coloresCreditos", coloresCreditos);
        
        List<Object[]> movimientosPorTipo = movimientoCuentaRepository.countMovimientosByTipo();
        
        List<String> etiquetasMovimientos = new java.util.ArrayList<>();
        List<Long> valoresMovimientos = new java.util.ArrayList<>();
        List<String> coloresMovimientos = new java.util.ArrayList<>();
        
        java.util.Map<String, String> coloresTipos = new java.util.HashMap<>();
        coloresTipos.put("DEPOSITO", "#00ff88");
        coloresTipos.put("RETIRO", "#ff1744");
        coloresTipos.put("TRANSFERENCIA", "#00bfff");
        coloresTipos.put("PAGO", "#ffc107");
        coloresTipos.put("ABONO", "#00ff88");
        coloresTipos.put("CARGO", "#ff1744");
        
        if (movimientosPorTipo != null && !movimientosPorTipo.isEmpty()) {
            for (Object[] row : movimientosPorTipo) {
                String tipo = (String) row[0];
                Long count = (Long) row[1];
                if (tipo != null && !tipo.isEmpty()) {
                    etiquetasMovimientos.add(tipo);
                    valoresMovimientos.add(count);
                    coloresMovimientos.add(coloresTipos.getOrDefault(tipo.toUpperCase(), "#6c757d"));
                }
            }
        } else {
            etiquetasMovimientos = List.of("DEPÓSITOS", "RETIROS", "TRANSFERENCIAS", "PAGOS");
            valoresMovimientos = List.of(0L, 0L, 0L, 0L);
            coloresMovimientos = List.of("#00ff88", "#ff1744", "#00bfff", "#ffc107");
        }
        
        modelo.addAttribute("etiquetasMovimientos", etiquetasMovimientos);
        modelo.addAttribute("valoresMovimientos", valoresMovimientos);
        modelo.addAttribute("coloresMovimientos", coloresMovimientos);
        
        // 🔥 LIMITAR A 10 SOLICITUDES Y RECORTAR FIRMAS
        List<SolicitudCreditoEntity> solicitudes = solicitudCreditoRepository.findAllByOrderByFechaDesc()
            .stream()
            .limit(10)
            .map(s -> {
                // Recortar la firma si es muy grande para que no sature
                if (s.getFirmaDigital() != null && s.getFirmaDigital().length() > 5000) {
                    s.setFirmaDigital(s.getFirmaDigital().substring(0, 5000) + "...");
                }
                return s;
            })
            .collect(Collectors.toList());
        modelo.addAttribute("solicitudes", solicitudes);
        
        return "admin";
    }

    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        List<UsuarioEntity> clientes = usuarioRepository.findAll().stream()
                .filter(u -> "CLIENTE".equals(u.getRol()))
                .collect(Collectors.toList());
        
        model.addAttribute("clientes", clientes);
        return "admin-clientes";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
            if (!"CLIENTE".equals(usuario.getRol())) {
                throw new RuntimeException("Solo se pueden eliminar clientes");
            }
            
            usuarioRepository.deleteById(id);
            
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado exitosamente");
            return "redirect:/admin/clientes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return "redirect:/admin/clientes";
        }
    }

    @GetMapping("/creditos")
    public String listarCreditos(Model model) {
        List<SolicitudCreditoEntity> solicitudes = solicitudCreditoRepository.findAllByOrderByFechaDesc();
        model.addAttribute("solicitudes", solicitudes);
        return "admin-creditos";
    }

    @GetMapping("/cuentas")
    public String listarCuentas(Model model) {
        List<CuentaEntity> cuentas = cuentaRepository.findAll();
        model.addAttribute("cuentas", cuentas);
        return "admin-cuentas";
    }

    @GetMapping("/movimientos")
    public String listarMovimientos(Model model) {
        List<MovimientoEntity> movimientos = movimientoCuentaRepository.findAllByOrderByFechaDesc();
        
        List<TransferenciaDTO> transferencias = new java.util.ArrayList<>();
        List<MovimientoEntity> otrosMovimientos = new java.util.ArrayList<>();
        
        if (movimientos != null && !movimientos.isEmpty()) {
            java.util.Map<String, TransferenciaDTO> transferenciasMap = new java.util.LinkedHashMap<>();
            
            for (MovimientoEntity mov : movimientos) {
                if ("TRANSFERENCIA_ENVIADA".equals(mov.getTipo())) {
                    String key = mov.getCuentaOrigen() + "_" + mov.getCuentaDestino() + "_" + mov.getMonto();
                    TransferenciaDTO dto = transferenciasMap.get(key);
                    if (dto == null) {
                        dto = new TransferenciaDTO(
                            mov.getId(),
                            mov.getCuentaOrigen(),
                            mov.getCuentaDestino(),
                            mov.getMonto(),
                            mov.getDescripcion(),
                            mov.getFecha(),
                            mov.getEstadoMovimiento(),
                            mov.getId(),
                            null
                        );
                        transferenciasMap.put(key, dto);
                    } else {
                        dto.setIdMovimientoEgreso(mov.getId());
                        dto.setEstado(mov.getEstadoMovimiento());
                    }
                } else if ("TRANSFERENCIA_RECIBIDA".equals(mov.getTipo())) {
                    String key = mov.getCuentaOrigen() + "_" + mov.getCuentaDestino() + "_" + mov.getMonto();
                    TransferenciaDTO dto = transferenciasMap.get(key);
                    if (dto != null) {
                        dto.setIdMovimientoIngreso(mov.getId());
                        if (!"PENDIENTE".equals(mov.getEstadoMovimiento()) && "PENDIENTE".equals(dto.getEstado())) {
                            dto.setEstado(mov.getEstadoMovimiento());
                        }
                    } else {
                        TransferenciaDTO nuevoDto = new TransferenciaDTO(
                            mov.getId(),
                            mov.getCuentaOrigen(),
                            mov.getCuentaDestino(),
                            mov.getMonto(),
                            mov.getDescripcion(),
                            mov.getFecha(),
                            mov.getEstadoMovimiento(),
                            null,
                            mov.getId()
                        );
                        transferenciasMap.put(key, nuevoDto);
                    }
                } else {
                    otrosMovimientos.add(mov);
                }
            }
            
            transferencias = new java.util.ArrayList<>(transferenciasMap.values());
            transferencias.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        }
        
        model.addAttribute("transferencias", transferencias);
        model.addAttribute("otrosMovimientos", otrosMovimientos);
        
        return "admin-movimientos";
    }

    @PostMapping("/movimientos/{id}/autorizar")
    public String autorizarMovimiento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MovimientoEntity movimiento = movimientoCuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
            
            movimiento.setEstadoMovimiento("AUTORIZADO");
            movimientoCuentaRepository.save(movimiento);
            
            redirectAttributes.addFlashAttribute("success", "Movimiento autorizado exitosamente");
            return "redirect:/admin/movimientos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al autorizar: " + e.getMessage());
            return "redirect:/admin/movimientos";
        }
    }

    @PostMapping("/movimientos/{id}/cancelar")
    public String cancelarMovimiento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MovimientoEntity movimiento = movimientoCuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
            
            movimiento.setEstadoMovimiento("CANCELADO");
            movimientoCuentaRepository.save(movimiento);
            
            redirectAttributes.addFlashAttribute("success", "Movimiento cancelado exitosamente");
            return "redirect:/admin/movimientos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
            return "redirect:/admin/movimientos";
        }
    }

    @PostMapping("/movimientos/{id}/confirmar")
    public String confirmarTransferencia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transferenciaService.confirmarTransferencia(id);
            redirectAttributes.addFlashAttribute("success", "Transferencia confirmada exitosamente. Ya no se puede cancelar.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar transferencia: " + e.getMessage());
        }
        return "redirect:/admin/movimientos";
    }

    @PostMapping("/movimientos/{id}/cancelar-transferencia")
    public String cancelarTransferencia(@PathVariable Long id,
                                        @RequestParam String motivo,
                                        RedirectAttributes redirectAttributes) {
        try {
            transferenciaService.cancelarTransferencia(id, motivo);
            redirectAttributes.addFlashAttribute("success", "Transferencia cancelada exitosamente. El dinero ha sido devuelto.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar transferencia: " + e.getMessage());
        }
        return "redirect:/admin/movimientos";
    }

    @PostMapping("/crear")
    public String crearcliente(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam Double montoApertura,
                               @RequestParam String nombre,
                               RedirectAttributes redirectAttributes) {

        if(username == null || username.trim().isEmpty()
        || password == null || password.trim().isEmpty()
        || nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Llenar todos los campos");
            return "redirect:/admin/dashboard";
        }
        if (montoApertura == null || montoApertura <= 0){
            redirectAttributes.addFlashAttribute("error", "No puede tener saldo 0");
            return "redirect:/admin/dashboard";
        }
        try{
            bancaService.crearClienteConCuenta(nombre, username, password, montoApertura);
            redirectAttributes.addFlashAttribute("success", "Cliente creado exitosamente");
            return "redirect:/admin/dashboard";
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/creditos/{id}/aprobar")
    public String aprobarCredito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bancaService.aprobarSolicitudCredito(id);
            redirectAttributes.addFlashAttribute("success", "Crédito aprobado y abonado a la cuenta del cliente");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/creditos/{id}/rechazar")
    public String rechazarCredito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bancaService.rechazarSolicitudCredito(id);
            redirectAttributes.addFlashAttribute("success", "Crédito rechazado correctamente");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}
