package com.example.login.Services;

import com.example.login.Models.EmployeSimple;
import com.example.login.Repositories.EmployeSimpleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final EmployeSimpleRepository employeSimpleRepository;

    public AuthenticationService(EmployeSimpleRepository employeSimpleRepository) {
        this.employeSimpleRepository = employeSimpleRepository;
    }

    @Transactional
    public void registerUser(EmployeSimple employeSimple) {
        // Generate ID if not provided
        if (employeSimple.getIdEmploye() == null) {
            employeSimple.setIdEmploye(UUID.randomUUID().toString());
        }
        
        // Ajouter {noop} pour indiquer que le mot de passe est en texte clair
        if (!employeSimple.getMotDePasse().startsWith("{noop}")) {
            employeSimple.setMotDePasse("{noop}" + employeSimple.getMotDePasse());
        }
        
        // Set creation date if not set
        if (employeSimple.getDateCreation() == null) {
            employeSimple.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
        }
        
        employeSimpleRepository.save(employeSimple);
    }
}