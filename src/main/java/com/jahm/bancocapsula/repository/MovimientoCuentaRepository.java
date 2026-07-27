package com.jahm.bancocapsula.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jahm.bancocapsula.entity.MovimientoEntity;

@Repository
public interface MovimientoCuentaRepository extends JpaRepository<MovimientoEntity, Long> {
    
    List<MovimientoEntity> findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(
            String cuentaOrigen, String cuentaDestino);

    List<MovimientoEntity> findByCuentaOrigenInOrCuentaDestinoInOrderByFechaDesc(
            List<String> cuentaOrigen, List<String> cuentaDestino);

    List<MovimientoEntity> findByCuentaOrigen(String cuentaOrigen);
    
    // Obtener todos los movimientos ordenados por fecha
    List<MovimientoEntity> findAllByOrderByFechaDesc();
    
    // Contar movimientos por tipo
    @Query("SELECT m.tipo, COUNT(m) FROM MovimientoEntity m GROUP BY m.tipo")
    List<Object[]> countMovimientosByTipo();
    
    // Buscar movimientos de ingreso correspondientes a una transferencia
    List<MovimientoEntity> findByCuentaOrigenAndCuentaDestinoAndMontoAndTipo(
            String cuentaOrigen, String cuentaDestino, Double monto, String tipo);
    
    // Buscar movimientos por tipo y estado (opcional, útil para filtros)
    List<MovimientoEntity> findByTipoAndEstadoMovimiento(String tipo, String estadoMovimiento);
    
    // Buscar transferencias enviadas que están autorizadas
    List<MovimientoEntity> findByTipoAndEstadoMovimientoOrderByFechaDesc(
            String tipo, String estadoMovimiento);
}