package com.ufpi.segt1.DTO;

import java.io.Serializable;

public record KeyDTO(
        Long id,
        Long ownerId,
        String name,
        String fileUrl,
        String password
) implements Serializable {}