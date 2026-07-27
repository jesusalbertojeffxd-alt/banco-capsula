package com.jahm.bancocapsula.service;

public class FondosInsuficientesException extends RuntimeException{
    public FondosInsuficientesException(String mensaje){
        super(mensaje);
    }

}
