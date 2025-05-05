package com.example.login.Repositories;

import com.example.login.Models.Configurateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigurateurRepository extends JpaRepository<Configurateur, String> {
    Optional<Configurateur> findByEmail(String email);
}