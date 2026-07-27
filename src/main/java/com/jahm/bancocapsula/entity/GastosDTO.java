package com.jahm.bancocapsula.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GastosDTO {
    private String categoria;
    private Double monto;
    private String colorHex;

    public GastosDTO() {}

    public GastosDTO(String categoria, Double monto, String colorHex) {
        this.categoria = categoria;
        this.monto = monto;
        this.colorHex = colorHex;
    }

    @JsonProperty("categoria")
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @JsonProperty("monto")
    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    // Exponer colorHex como 'color' en JSON
    @JsonProperty("color")
    public String getColorHex() {
        return colorHex;
    }

    @JsonProperty("color")
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}