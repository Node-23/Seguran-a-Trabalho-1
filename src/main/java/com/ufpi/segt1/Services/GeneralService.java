package com.ufpi.segt1.Services;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;

public class GeneralService {
    public static String getConstrainField(DataIntegrityViolationException ex){
        String exceptionMessage = ex.getMessage();
        int startIndex = exceptionMessage.indexOf("(") + 1;
        int endIndex = exceptionMessage.indexOf(")");

        if (endIndex != -1) {
            String fieldAndValue = exceptionMessage.substring(startIndex, endIndex);
            String[] parts = fieldAndValue.split("=");
            String field = Arrays.toString(parts).replace("[", "").replace("]", "").replace("_", "");
            return field.equals("name") ? "Nome" : field.substring(0, 1).toUpperCase() + field.substring(1);
        }
        return "Campo";
    }
}
