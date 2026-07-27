package com.jahm.bancocapsula.repository;

import com.jahm.bancocapsula.entity.CuentaEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
    
    // ============================================================
    // MÉTODOS EXISTENTES (ya los tienes)
    // ============================================================
    
    // Buscar cuenta por CLABE (esto ya existe en tu entidad)
    Optional<CuentaEntity> findByClabe(String clabe);
    
    // Suma total de todos los saldos
    @Query("SELECT SUM(c.saldo) FROM CuentaEntity c")
    Double sumSaldo();
    
    // ============================================================
    // MÉTODOS ADICIONALES
    // ============================================================
    
    // Buscar cuentas por usuario
    List<CuentaEntity> findByUsuario(UsuarioEntity usuario);
    
    // Buscar cuentas por estado
    List<CuentaEntity> findByEstado(String estado);
    
    // Buscar cuentas activas excepto la del usuario especificado
    @Query("SELECT c FROM CuentaEntity c WHERE c.estado = 'ACTIVA' AND c.usuario.id != :usuarioId")
    List<CuentaEntity> findCuentasActivasExceptoUsuario(Long usuarioId);
    
    // Buscar cuentas activas de un usuario específico
    @Query("SELECT c FROM CuentaEntity c WHERE c.usuario.id = :usuarioId AND c.estado = 'ACTIVA'")
    List<CuentaEntity> findCuentasActivasByUsuarioId(Long usuarioId);
    
    // Contar cuentas activas
    @Query("SELECT COUNT(c) FROM CuentaEntity c WHERE c.estado = 'ACTIVA'")
    long countCuentasActivas();
    
    // Buscar cuentas por tipo (si tienes campo tipo)
    List<CuentaEntity> findByTipo(String tipo);
}