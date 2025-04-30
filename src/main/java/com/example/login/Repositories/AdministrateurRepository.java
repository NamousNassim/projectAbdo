package com.example.login.Repositories;

import com.example.login.Models.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrateurRepository extends JpaRepository<Administrateur, String> {
}