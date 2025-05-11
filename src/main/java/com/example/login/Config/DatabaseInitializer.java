package com.example.login.Config;

import com.example.login.Models.Administrateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Role;
import com.example.login.Repositories.AdministrateurRepository;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RoleRepository;
import com.example.login.Repositories.ConfigurateurRepository;
import com.example.login.Repositories.RhRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeSimpleRepository employeSimpleRepository;
    private final AdministrateurRepository administrateurRepository;
    private final ConfigurateurRepository configurateurRepository;
    private final RhRepository rhRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public DatabaseInitializer(
            RoleRepository roleRepository, 
            EmployeSimpleRepository employeSimpleRepository,
            AdministrateurRepository administrateurRepository,
            ConfigurateurRepository configurateurRepository,
            RhRepository rhRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.employeSimpleRepository = employeSimpleRepository;
        this.administrateurRepository = administrateurRepository;
        this.configurateurRepository = configurateurRepository;
        this.rhRepository = rhRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting database initialization...");
        
        // Check for duplicate admin users and clean if necessary
        cleanDuplicateAdmins();
        
        // Create fresh roles
        initializeRoles();
        
        // Create default admin user only if it doesn't exist
        if (!adminUserExists()) {
            createDefaultAdmin();
        } else {
            System.out.println("Admin user already exists, skipping creation");
        }
        
        System.out.println("Database initialization completed successfully");
    }
    
    private boolean adminUserExists() {
        // Check in both EmployeSimple and Administrateur tables
        Optional<EmployeSimple> employeAdmin = employeSimpleRepository.findByEmailPro("admin@company.com");
        Optional<Administrateur> administrateur = administrateurRepository.findByEmail("admin@company.com");
        
        return employeAdmin.isPresent() || administrateur.isPresent();
    }
    
    private void cleanDuplicateAdmins() {
        try {
            // Check for multiple admin users with the same email
            List<Administrateur> admins = administrateurRepository.findAllByEmail("admin@company.com");
            
            if (admins.size() > 1) {
                System.out.println("Found " + admins.size() + " duplicate admin users. Cleaning duplicates...");
                
                // Keep the first one, delete the rest
                for (int i = 1; i < admins.size(); i++) {
                    Administrateur admin = admins.get(i);
                    String idToDelete = admin.getIdAdministrateur();
                    
                    // Delete from both tables
                    administrateurRepository.deleteById(idToDelete);
                    employeSimpleRepository.deleteById(idToDelete);
                }
                
                System.out.println("Duplicate admin users cleaned successfully");
            }
        } catch (Exception e) {
            System.err.println("Error while cleaning duplicate admins: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeRoles() {
        createRoleIfNotExists("ADMIN", "Administrator role");
        createRoleIfNotExists("RH", "Human Resources role");
        createRoleIfNotExists("CONFIGURATEUR", "Configurator role");
        createRoleIfNotExists("EMPLOYE", "Basic employee role");
    }
    
    private void createRoleIfNotExists(String idRole, String description) {
        Optional<Role> existingRole = roleRepository.findByIdRole(idRole);
        if (existingRole.isPresent()) {
            System.out.println("Role already exists: " + idRole);
            return;
        }
        
        Role role = new Role();
        role.setIdRole(idRole);
        role.setNomRole(idRole);
        role.setDescription(description);
        roleRepository.save(role);
        System.out.println("Created role: " + idRole);
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
        
        // Use BCrypt to encode the password
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Admin password created with BCrypt encoding");
        adminEmployee.setMotDePasse(encodedPassword);
        
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
        
        System.out.println("Created default admin user: admin@company.com with password: admin123");
    }
}