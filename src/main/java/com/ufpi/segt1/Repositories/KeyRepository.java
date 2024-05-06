package com.ufpi.segt1.Repositories;

import com.ufpi.segt1.Models.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyRepository extends JpaRepository<Key, Long> {
    Optional<Key> findKeyById(Long id);
}
