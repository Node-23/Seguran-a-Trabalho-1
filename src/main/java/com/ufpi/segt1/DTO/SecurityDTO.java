package com.ufpi.segt1.DTO;

import java.io.Serializable;

public record SecurityDTO(
        Long keyId,
        String message,
        String password
) implements Serializable {}
