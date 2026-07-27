package com.jahm.bancocapsula.service;

import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.CuentaRepository;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransferenciaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoCuentaRepository movimientoCuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Realiza una transferencia entre cuentas
     * El dinero se descuenta del origen y se acredita al destino INMEDIATAMENTE
     * Pero queda en estado PENDIENTE para que el admin lo autorice o cancele
     */
    @Transactional
    public void realizarTransferencia(String clabeOrigen, String clabeDestino, 
                                      Double monto, String descripcion) {
        
        // Buscar cuentas por CLABE
        CuentaEntity cuentaOrigen = cuentaRepository.findByClabe(clabeOrigen)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
        
        CuentaEntity cuentaDestino = cuentaRepository.findByClabe(clabeDestino)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));
        
        // Validar saldo
        if (cuentaOrigen.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente");
        }
        
        // REALIZAR LA TRANSFERENCIA (DESCUENTO Y ACREDITO) - INMEDIATO
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - monto);
        cuentaDestino.setSaldo(cuentaDestino.getSaldo() + monto);
        
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);
        
        // REGISTRAR MOVIMIENTO DE EGRESO (Origen) - ESTADO PENDIENTE
        MovimientoEntity egreso = new MovimientoEntity();
        egreso.setCuentaOrigen(clabeOrigen);
        egreso.setCuentaDestino(clabeDestino);
        egreso.setMonto(monto);
        egreso.setTipo("TRANSFERENCIA_ENVIADA");
        egreso.setDescripcion(descripcion != null ? descripcion : "Transferencia enviada");
        egreso.setFecha(LocalDate.now());
        egreso.setEstadoMovimiento("PENDIENTE");
        movimientoCuentaRepository.save(egreso);
        
        // REGISTRAR MOVIMIENTO DE INGRESO (Destino) - ESTADO PENDIENTE
        MovimientoEntity ingreso = new MovimientoEntity();
        ingreso.setCuentaOrigen(clabeOrigen);
        ingreso.setCuentaDestino(clabeDestino);
        ingreso.setMonto(monto);
        ingreso.setTipo("TRANSFERENCIA_RECIBIDA");
        ingreso.setDescripcion(descripcion != null ? descripcion : "Transferencia recibida");
        ingreso.setFecha(LocalDate.now());
        ingreso.setEstadoMovimiento("PENDIENTE");
        movimientoCuentaRepository.save(ingreso);
    }

    /**
     * CONFIRMAR transferencia (admin autoriza)
     * El movimiento queda como AUTORIZADO y ya no se puede cancelar
     */
    @Transactional
    public void confirmarTransferencia(Long idMovimientoEgreso) {
        MovimientoEntity egreso = movimientoCuentaRepository.findById(idMovimientoEgreso)
            .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        
        if (!"TRANSFERENCIA_ENVIADA".equals(egreso.getTipo())) {
            throw new RuntimeException("Solo se pueden confirmar transferencias enviadas");
        }
        
        if (!"PENDIENTE".equals(egreso.getEstadoMovimiento())) {
            throw new RuntimeException("Solo se pueden confirmar movimientos pendientes");
        }
        
        egreso.setEstadoMovimiento("AUTORIZADO");
        movimientoCuentaRepository.save(egreso);
        
        List<MovimientoEntity> movimientosIngreso = movimientoCuentaRepository
            .findByCuentaOrigenAndCuentaDestinoAndMontoAndTipo(
                egreso.getCuentaOrigen(), egreso.getCuentaDestino(), 
                egreso.getMonto(), "TRANSFERENCIA_RECIBIDA");
        
        if (!movimientosIngreso.isEmpty()) {
            MovimientoEntity ingreso = movimientosIngreso.get(0);
            ingreso.setEstadoMovimiento("AUTORIZADO");
            movimientoCuentaRepository.save(ingreso);
        }
    }

    /**
     * CANCELAR una transferencia (admin)
     * El dinero regresa al origen y se descuenta del destino
     * Si el destino ya gastó el dinero, queda en SALDO NEGATIVO (ROJO)
     */
    @Transactional
    public void cancelarTransferencia(Long idMovimientoEgreso, String motivo) {
        // Buscar el movimiento de egreso original
        MovimientoEntity egresoOriginal = movimientoCuentaRepository.findById(idMovimientoEgreso)
            .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        
        // Verificar que sea una transferencia enviada
        if (!"TRANSFERENCIA_ENVIADA".equals(egresoOriginal.getTipo())) {
            throw new RuntimeException("Solo se pueden cancelar transferencias enviadas");
        }
        
        // Verificar que esté PENDIENTE (solo se puede cancelar si está pendiente)
        if (!"PENDIENTE".equals(egresoOriginal.getEstadoMovimiento())) {
            throw new RuntimeException("Solo se pueden cancelar movimientos pendientes");
        }
        
        String clabeOrigen = egresoOriginal.getCuentaOrigen();
        String clabeDestino = egresoOriginal.getCuentaDestino();
        Double monto = egresoOriginal.getMonto();
        
        // Buscar cuentas
        CuentaEntity cuentaOrigen = cuentaRepository.findByClabe(clabeOrigen)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
        
        CuentaEntity cuentaDestino = cuentaRepository.findByClabe(clabeDestino)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));
        
        // ✅ NUEVO: Verificar si el destino tiene suficiente saldo
        Double saldoDestino = cuentaDestino.getSaldo();
        Double nuevoSaldoDestino;
        boolean saldoNegativo = false;
        
        if (saldoDestino >= monto) {
            // Si tiene suficiente, se descuenta normalmente
            nuevoSaldoDestino = saldoDestino - monto;
        } else {
            // Si NO tiene suficiente, queda en SALDO NEGATIVO
            nuevoSaldoDestino = saldoDestino - monto;
            saldoNegativo = true;
            System.out.println("⚠️ SALDO NEGATIVO en cuenta destino: " + nuevoSaldoDestino);
        }
        
        // REVERTIR LA TRANSFERENCIA
        cuentaDestino.setSaldo(nuevoSaldoDestino);
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() + monto);
        
        cuentaRepository.save(cuentaDestino);
        cuentaRepository.save(cuentaOrigen);
        
        // ACTUALIZAR EL MOVIMIENTO ORIGINAL A CANCELADO
        egresoOriginal.setEstadoMovimiento("CANCELADO");
        movimientoCuentaRepository.save(egresoOriginal);
        
        // Buscar y actualizar el movimiento de ingreso correspondiente
        List<MovimientoEntity> movimientosIngreso = movimientoCuentaRepository
            .findByCuentaOrigenAndCuentaDestinoAndMontoAndTipo(
                clabeOrigen, clabeDestino, monto, "TRANSFERENCIA_RECIBIDA");
        
        if (!movimientosIngreso.isEmpty()) {
            MovimientoEntity ingreso = movimientosIngreso.get(0);
            ingreso.setEstadoMovimiento("CANCELADO");
            movimientoCuentaRepository.save(ingreso);
        }
        
        // REGISTRAR MOVIMIENTO DE REVERSO (EGRESO del destino)
        MovimientoEntity reversoEgreso = new MovimientoEntity();
        reversoEgreso.setCuentaOrigen(clabeDestino);
        reversoEgreso.setCuentaDestino(clabeOrigen);
        reversoEgreso.setMonto(monto);
        reversoEgreso.setTipo("CANCELACION_TRANSFERENCIA");
        String descripcionReverso = "Cancelación de transferencia: " + motivo;
        if (saldoNegativo) {
            descripcionReverso += " (⚠️ SALDO NEGATIVO: $" + nuevoSaldoDestino + ")";
        }
        reversoEgreso.setDescripcion(descripcionReverso);
        reversoEgreso.setFecha(LocalDate.now());
        reversoEgreso.setEstadoMovimiento("AUTORIZADO");
        movimientoCuentaRepository.save(reversoEgreso);
        
        // REGISTRAR MOVIMIENTO DE REVERSO (INGRESO al origen)
        MovimientoEntity reversoIngreso = new MovimientoEntity();
        reversoIngreso.setCuentaOrigen(clabeDestino);
        reversoIngreso.setCuentaDestino(clabeOrigen);
        reversoIngreso.setMonto(monto);
        reversoIngreso.setTipo("CANCELACION_TRANSFERENCIA");
        reversoIngreso.setDescripcion("Devolución por cancelación: " + motivo);
        reversoIngreso.setFecha(LocalDate.now());
        reversoIngreso.setEstadoMovimiento("AUTORIZADO");
        movimientoCuentaRepository.save(reversoIngreso);
    }
}