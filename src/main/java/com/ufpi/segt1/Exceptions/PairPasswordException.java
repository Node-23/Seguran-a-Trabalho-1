package com.ufpi.segt1.Exceptions;

public class PairPasswordException extends RuntimeException{
    public PairPasswordException() {
        super("Senha incorreta!");
    }
}
