package com.ufpi.segt1.Exceptions;

public class KeyNotFoundException extends RuntimeException {
    public KeyNotFoundException() {
        super("Chave não encontrada");
    }
}
