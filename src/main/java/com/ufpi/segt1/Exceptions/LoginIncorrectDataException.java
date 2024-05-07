package com.ufpi.segt1.Exceptions;

public class LoginIncorrectDataException extends RuntimeException{
    public LoginIncorrectDataException() {
        super("Email ou senha incorretos");
    }
}
