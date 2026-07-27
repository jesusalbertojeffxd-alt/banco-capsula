package com.jahm.bancocapsula.service;

import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.CuentaRepository;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.SolicitudCreditoRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BancaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoCuentaRepository movimientoCuentaRepository;

    @Autowired
    private SolicitudCreditoRepository solicitudCreditoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============================================================
    // CREAR CLIENTE CON CUENTA
    // ============================================================
    @Transactional
    public void crearClienteConCuenta(String nombre, String username, String password, Double montoApertura) {
        // Crear usuario
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        // Crear cuenta
        CuentaEntity cuenta = new CuentaEntity();
        String clabe = generarClabe();
        cuenta.setClabe(clabe);
        cuenta.setSaldo(montoApertura);
        cuenta.setUsuario(usuario);
        cuenta.setEstado("ACTIVA");
        cuentaRepository.save(cuenta);

        // Registrar movimiento de apertura - USANDO LocalDate
        MovimientoEntity movimiento = new MovimientoEntity();
        movimiento.setCuentaOrigen("BANCO");
        movimiento.setCuentaDestino(clabe);
        movimiento.setMonto(montoApertura);
        movimiento.setTipo("DEPOSITO");
        movimiento.setDescripcion("Apertura de cuenta");
        movimiento.setFecha(LocalDate.now()); // ✅ CORREGIDO: LocalDate
        movimiento.setEstadoMovimiento("AUTORIZADO");
        movimientoCuentaRepository.save(movimiento);
    }

    private String generarClabe() {
        String prefix = "CL";
        String numero = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        return prefix + numero + "000000";
    }

    // ============================================================
    // APROBAR SOLICITUD DE CRÉDITO
    // ============================================================
    @Transactional
    public void aprobarSolicitudCredito(Long idSolicitud) {
        SolicitudCreditoEntity solicitud = solicitudCreditoRepository.findById(idSolicitud)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud no está pendiente");
        }

        CuentaEntity cuenta = cuentaRepository.findByUsuario(solicitud.getUsuario())
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("El usuario no tiene cuenta"));

        cuenta.setSaldo(cuenta.getSaldo() + solicitud.getMontoSolicitado());
        cuentaRepository.save(cuenta);

        MovimientoEntity movimiento = new MovimientoEntity();
        movimiento.setCuentaOrigen("BANCO");
        movimiento.setCuentaDestino(cuenta.getClabe());
        movimiento.setMonto(solicitud.getMontoSolicitado());
        movimiento.setTipo("ABONO");
        movimiento.setDescripcion("Crédito aprobado");
        movimiento.setFecha(LocalDate.now()); // ✅ CORREGIDO: LocalDate
        movimiento.setEstadoMovimiento("AUTORIZADO");
        movimientoCuentaRepository.save(movimiento);

        solicitud.setEstado("AUTORIZADO");
        solicitudCreditoRepository.save(solicitud);
    }

    // ============================================================
    // RECHAZAR SOLICITUD DE CRÉDITO
    // ============================================================
    @Transactional
    public void rechazarSolicitudCredito(Long idSolicitud) {
        SolicitudCreditoEntity solicitud = solicitudCreditoRepository.findById(idSolicitud)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud no está pendiente");
        }

        solicitud.setEstado("RECHAZADO");
        solicitudCreditoRepository.save(solicitud);
    }

    // ============================================================
    // GUARDAR SOLICITUD DE CRÉDITO (desde el cliente)
    // ============================================================
    @Transactional
    public void guardarSolicitudCredito(String username, Double monto, String firmaBase64) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SolicitudCreditoEntity solicitud = new SolicitudCreditoEntity();
        solicitud.setUsuario(usuario);
        solicitud.setMontoSolicitado(monto);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFecha(LocalDateTime.now());
        solicitud.setFirmaDigital(firmaBase64);

        solicitudCreditoRepository.save(solicitud);
    }

    // ============================================================
    // VER MIS SOLICITUDES DE CRÉDITO
    // ============================================================
    public List<SolicitudCreditoEntity> getSolicitudesByUsuario(UsuarioEntity usuario) {
        return solicitudCreditoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }
}