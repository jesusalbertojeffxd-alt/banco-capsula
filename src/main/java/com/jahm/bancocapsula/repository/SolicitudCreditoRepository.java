package com.jahm.bancocapsula.repository;

import com.jahm.bancocapsula.entity.SolicitudCreditoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCreditoEntity, Long> {
    // Métodos personalizados
    List<SolicitudCreditoEntity> findByUsuarioOrderByFechaDesc(UsuarioEntity usuario);
    List<SolicitudCreditoEntity> findAllByOrderByFechaDesc();
}