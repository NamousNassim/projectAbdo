package com.example.login.Controllers;

import com.example.login.Models.Administrateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Role;
import com.example.login.Repositories.AdministrateurRepository;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/setup")
public class SetupController {

    private final RoleRepository roleRepository;
    private final EmployeSimpleRepository employeRepository;
    private final AdministrateurRepository adminRepository;
    
    @Value("${app.setup.enabled:false}")
    private boolean setupEnabled;
    
    @Value("${app.setup.key:}")
    private String setupKey;

    @Autowired
    public SetupController(
            RoleRepository roleRepository,
            EmployeSimpleRepository employeRepository,
            AdministrateurRepository adminRepository) {
        this.roleRepository = roleRepository;
        this.employeRepository = employeRepository;
        this.adminRepository = adminRepository;
    }

    @PostMapping("/create-admin")
    @Transactional
    public ResponseEntity<?> createAdmin(
            @RequestBody EmployeSimple employeSimple,
            @RequestParam(required = false) String key) {
        
        // Security check - only allow in development or with correct setup key
        if (!setupEnabled && (setupKey.isEmpty() || !setupKey.equals(key))) {
            return ResponseEntity.status(403).body("Setup not enabled or invalid key");
        }
        
        try {
            // Create roles if they don't exist
            createRoleIfNotExists("ADMIN", "Administrator role");
            createRoleIfNotExists("RH", "Human Resources role");
            createRoleIfNotExists("CONFIGURATEUR", "Configurator role");
            createRoleIfNotExists("EMPLOYE", "Basic employee role");
            
            // Get admin role
            Role adminRole = roleRepository.findByIdRole("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            // Set admin role
            employeSimple.setRole(adminRole);
            
            // Generate ID if not provided
            if (employeSimple.getIdEmploye() == null) {
                employeSimple.setIdEmploye(UUID.randomUUID().toString());
            }
            
            // Set password with {noop} prefix for plain text
            if (!employeSimple.getMotDePasse().startsWith("{noop}")) {
                employeSimple.setMotDePasse("{noop}" + employeSimple.getMotDePasse());
            }
            
            // Save the employee
            employeRepository.save(employeSimple);
            
            // Create administrateur entry
            Administrateur admin = new Administrateur();
            admin.setIdAdministrateur(employeSimple.getIdEmploye());
            admin.setNom(employeSimple.getNom());
            admin.setPrenom(employeSimple.getPrenom());
            admin.setEmail(employeSimple.getEmailPro());
            admin.setDateModification(new Date());
            
            // Save the admin
            adminRepository.save(admin);
            
            return ResponseEntity.ok("Admin created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error creating admin: " + e.getMessage());
        }
    }
    
    private void createRoleIfNotExists(String idRole, String description) {
        if (roleRepository.findByIdRole(idRole).isEmpty()) {
            Role role = new Role();
            role.setIdRole(idRole);
            role.setNomRole(idRole);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}