package com.ufpi.segt1.Infra;

import com.ufpi.segt1.DTO.ExceptionDTO;
import com.ufpi.segt1.Exceptions.FieldAlreadyInUseException;
import com.ufpi.segt1.Exceptions.KeyNotFoundException;
import com.ufpi.segt1.Exceptions.PasswordRulesException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(PasswordRulesException.class)
    public ResponseEntity InvalidPassword(PasswordRulesException exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }
    @ExceptionHandler(FieldAlreadyInUseException.class)
    public ResponseEntity ThreatDuplicationEntry(FieldAlreadyInUseException exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }
    @ExceptionHandler(KeyNotFoundException.class)
    public ResponseEntity UserNotFound(KeyNotFoundException exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "404");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity threatGeneralException(Exception exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "500");
        return ResponseEntity.internalServerError().body(exceptionDTO);
    }
}
