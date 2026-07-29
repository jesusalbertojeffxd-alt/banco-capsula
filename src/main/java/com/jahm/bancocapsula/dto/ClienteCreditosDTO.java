package com.jahm.bancocapsula.dto;

public class ClienteCreditosDTO {
    private Long id;
    private String nombre;
    private String username;
    private int totalCreditos;
    private double montoTotal;

    // Constructor vacío
    public ClienteCreditosDTO() {}

    // Constructor con parámetros
    public ClienteCreditosDTO(Long id, String nombre, String username, int totalCreditos, double montoTotal) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.totalCreditos = totalCreditos;
        this.montoTotal = montoTotal;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(int totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}