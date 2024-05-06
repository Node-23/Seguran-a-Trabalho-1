package com.ufpi.segt1.DTO;

import java.io.Serializable;

public record KeyDTO(
        Long id,
        String name,
        String pub,
        String priv,
        String password
) implements Serializable {}