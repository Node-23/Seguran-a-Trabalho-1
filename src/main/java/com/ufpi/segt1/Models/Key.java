package com.ufpi.segt1.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    private String fileUrl;
    private String password;
    @ManyToOne
    @JsonBackReference
    private User owner;

    public Key(KeyDTO key, User owner) {
        this.id = key.id();
        this.name = key.name();
        this.fileUrl = key.fileUrl();
        this.password = key.password();
        this.owner = owner;
    }
}
