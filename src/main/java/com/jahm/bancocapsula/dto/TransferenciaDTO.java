package com.jahm.bancocapsula.dto;

import java.time.LocalDate;

public class TransferenciaDTO {
    private Long id;
    private String cuentaOrigen;
    private String cuentaDestino;
    private Double monto;
    private String descripcion;
    private LocalDate fecha;
    private String estado;
    private Long idMovimientoEgreso;
    private Long idMovimientoIngreso;

    public TransferenciaDTO() {}

    public TransferenciaDTO(Long id, String cuentaOrigen, String cuentaDestino, 
                           Double monto, String descripcion, LocalDate fecha, 
                           String estado, Long idMovimientoEgreso, Long idMovimientoIngreso) {
        this.id = id;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estado = estado;
        this.idMovimientoEgreso = idMovimientoEgreso;
        this.idMovimientoIngreso = idMovimientoIngreso;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(String cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }
    
    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String cuentaDestino) { this.cuentaDestino = cuentaDestino; }
    
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Long getIdMovimientoEgreso() { return idMovimientoEgreso; }
    public void setIdMovimientoEgreso(Long idMovimientoEgreso) { this.idMovimientoEgreso = idMovimientoEgreso; }
    
    public Long getIdMovimientoIngreso() { return idMovimientoIngreso; }
    public void setIdMovimientoIngreso(Long idMovimientoIngreso) { this.idMovimientoIngreso = idMovimientoIngreso; }
}