package com.ufpi.segt1.DTO;

import java.io.Serializable;

public record LoginDTO(
        String email,
        String password
) implements Serializable  {}
