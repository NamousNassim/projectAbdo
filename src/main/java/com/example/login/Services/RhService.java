package com.example.login.Services;

import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Rh;
import com.example.login.Models.Role;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RhRepository;
import com.example.login.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RhService {

    private final RhRepository rhRepository;
    private final EmployeSimpleRepository employeRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public RhService(RhRepository rhRepository,
                     EmployeSimpleRepository employeRepository,
                     RoleRepository roleRepository) {
        this.rhRepository = rhRepository;
        this.employeRepository = employeRepository;
        this.roleRepository = roleRepository;
    }

    public List<Rh> getAllRhs() {
        return rhRepository.findAll();
    }

    public Rh getRh(String id) {
        return rhRepository.findById(id).orElse(null);
    }

    @Transactional
    public Rh createRh(Rh rh, EmployeSimple employeSimple) {
        // Set role for employee
        Role role = roleRepository.findByIdRole("RH")
                .orElseThrow(() -> new RuntimeException("Role RH not found"));
        employeSimple.setRole(role);

        // Set password with {noop} prefix
        employeSimple.setMotDePasse("{noop}" + employeSimple.getMotDePasse());

        // Generate ID if not provided
        if (employeSimple.getIdEmploye() == null) {
            employeSimple.setIdEmploye(UUID.randomUUID().toString());
        }

        // Save employee first
        employeRepository.save(employeSimple);

        // Configure the RH
        rh.setIdEmploye(employeSimple.getIdEmploye());
        rh.setDateCreation(new Date());

        return rhRepository.save(rh);
    }
}