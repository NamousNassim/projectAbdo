package com.example.login.Services;

import com.example.login.Models.Configurateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Role;
import com.example.login.Repositories.ConfigurateurRepository;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class ConfigurateurService {

    private final ConfigurateurRepository configurateurRepository;
    private final EmployeSimpleRepository employeRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public ConfigurateurService(ConfigurateurRepository configurateurRepository,
                                EmployeSimpleRepository employeRepository,
                                RoleRepository roleRepository) {
        this.configurateurRepository = configurateurRepository;
        this.employeRepository = employeRepository;
        this.roleRepository = roleRepository;
    }

    public List<Configurateur> getAllConfigurateurs() {
        return configurateurRepository.findAll();
    }

    public Configurateur getConfigurateur(String id) {
        return configurateurRepository.findById(id).orElse(null);
    }

    @Transactional
    public Configurateur createConfigurateur(Configurateur configurateur, EmployeSimple employeSimple) {
        // Set role for employee
        Role role = roleRepository.findByIdRole("CONFIGURATEUR")
                .orElseThrow(() -> new RuntimeException("Role CONFIGURATEUR not found"));
        employeSimple.setRole(role);

        // Set password with {noop} prefix for plain text (you should use a proper encoder in production)
        employeSimple.setMotDePasse("{noop}" + employeSimple.getMotDePasse());

        // Generate ID if not provided
        if (employeSimple.getIdEmploye() == null) {
            employeSimple.setIdEmploye(UUID.randomUUID().toString());
        }

        // Save employee first
        employeRepository.save(employeSimple);

        // Configure the configurateur
        configurateur.setIdConfiguration(employeSimple.getIdEmploye());
        configurateur.setDateModification(new Timestamp(System.currentTimeMillis()));

        return configurateurRepository.save(configurateur);
    }
}