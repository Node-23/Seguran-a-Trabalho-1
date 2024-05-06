package com.ufpi.segt1.Models;

import com.ufpi.segt1.DTO.KeyDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "keys")
@Table(name = "keys")
public class Key implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;
    private String pub;
    private String priv;
    private String password;

    public Key(KeyDTO key) {
        this.id = key.id();
        this.name = key.name();
        this.pub = key.pub();
        this.priv = key.priv();
        this.password = key.password();
    }
}
