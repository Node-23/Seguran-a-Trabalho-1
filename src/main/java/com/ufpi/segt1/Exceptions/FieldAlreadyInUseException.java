package com.ufpi.segt1.Exceptions;

public class FieldAlreadyInUseException extends RuntimeException {

    public FieldAlreadyInUseException(String field) {
        super(field + " já cadastrado");
    }
}
