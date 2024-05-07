package com.ufpi.segt1.DTO;

import java.io.Serializable;

public record UserDTO(
        Long id,
        String name,
        String username,
        String email,
        String password
) implements Serializable {}