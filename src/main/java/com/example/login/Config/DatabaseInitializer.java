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
        
        // Delete all existing data
        cleanDatabase();
        
        // Create fresh roles
        initializeRoles();
        
        // Create default admin user
        createDefaultAdmin();
        
        System.out.println("Database initialization completed successfully");
    }
    
    private void cleanDatabase() {
        System.out.println("Cleaning database...");
        
        try {
            // Temporarily disable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Delete many-to-many junction tables first
            jdbcTemplate.execute("DELETE FROM societe_configurateur");
            jdbcTemplate.execute("DELETE FROM administrateur_configurateur");
            
            // Delete entity tables in correct order
            jdbcTemplate.execute("DELETE FROM configurateur");
            jdbcTemplate.execute("DELETE FROM rh");
            jdbcTemplate.execute("DELETE FROM administrateur");
            jdbcTemplate.execute("DELETE FROM employe_simple");
            jdbcTemplate.execute("DELETE FROM role");
            
            // Re-enable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            System.out.println("Database cleaned successfully using direct SQL");
        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
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