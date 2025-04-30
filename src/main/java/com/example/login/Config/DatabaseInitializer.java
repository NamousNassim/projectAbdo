package com.example.login.Config;

import com.example.login.Models.Administrateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Role;
import com.example.login.Repositories.AdministrateurRepository;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeSimpleRepository employeSimpleRepository;
    private final AdministrateurRepository administrateurRepository;
    
    @Autowired
    public DatabaseInitializer(
            RoleRepository roleRepository, 
            EmployeSimpleRepository employeSimpleRepository,
            AdministrateurRepository administrateurRepository) {
        this.roleRepository = roleRepository;
        this.employeSimpleRepository = employeSimpleRepository;
        this.administrateurRepository = administrateurRepository;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        // Initialize roles
        initializeRoles();
        
        // Check if admin exists
        Optional<EmployeSimple> existingAdmin = employeSimpleRepository.findByEmailPro("admin@company.com");
        
        if (existingAdmin.isEmpty()) {
            // Create default admin user
            createDefaultAdmin();
        }
    }
    
    private void initializeRoles() {
        createRoleIfNotExists("ADMIN", "Administrator role");
        createRoleIfNotExists("RH", "Human Resources role");
        createRoleIfNotExists("CONFIGURATEUR", "Configurator role");
        createRoleIfNotExists("EMPLOYE", "Basic employee role");
    }
    
    private void createRoleIfNotExists(String idRole, String description) {
        if (roleRepository.findByIdRole(idRole).isEmpty()) {
            Role role = new Role();
            role.setIdRole(idRole);
            role.setNomRole(idRole);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.println("Created role: " + idRole);
        }
    }
    
    @Transactional
    private void createDefaultAdmin() {
        // Get admin role
        Role adminRole = roleRepository.findByIdRole("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
                
        // Create employee record for admin
        String adminId = UUID.randomUUID().toString();
        EmployeSimple adminEmployee = new EmployeSimple();
        adminEmployee.setIdEmploye(adminId);
        adminEmployee.setNom("Admin");
        adminEmployee.setPrenom("Super");
        adminEmployee.setEmailPro("admin@company.com");
        adminEmployee.setMotDePasse("{noop}admin123"); // Set a strong password in production
        adminEmployee.setRole(adminRole);
        adminEmployee.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
        
        // Save employee
        employeSimpleRepository.save(adminEmployee);
        
        // Create admin record
        Administrateur admin = new Administrateur();
        admin.setIdAdministrateur(adminId);
        admin.setNom("Admin");
        admin.setPrenom("Super");
        admin.setEmail("admin@company.com");
        admin.setDateModification(new Date());
        
        // Save admin
        administrateurRepository.save(admin);
        
        System.out.println("Created default admin user: admin@company.com");
    }
}