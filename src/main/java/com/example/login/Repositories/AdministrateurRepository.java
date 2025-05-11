package com.example.login.Repositories;

import com.example.login.Models.Administrateur;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrateurRepository extends JpaRepository<Administrateur, String> {
    Optional<Administrateur> findByEmail(String email);
List<Administrateur> findAllByEmail(String email);
}