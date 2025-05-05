package com.example.login.Services;

import com.example.login.Models.Configurateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Models.Role;
import com.example.login.Repositories.ConfigurateurRepository;
import com.example.login.Repositories.EmployeSimpleRepository;
import com.example.login.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ConfigurateurService(ConfigurateurRepository configurateurRepository,
                                EmployeSimpleRepository employeRepository,
                                RoleRepository roleRepository,
                                PasswordEncoder passwordEncoder) {
        this.configurateurRepository = configurateurRepository;
        this.employeRepository = employeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all configurateurs
     * @return List of all configurateurs
     */
    public List<Configurateur> listConfigurateurs() {
        return configurateurRepository.findAll();
    }

    /**
     * Get configurateur by ID
     * @param id The configurateur ID
     * @return The configurateur if found, null otherwise
     */
    public Configurateur getConfigurateurById(String id) {
        return configurateurRepository.findById(id).orElse(null);
    }

    /**
     * Create a new configurateur
     * @param configurateur Configurateur data
     * @param employeSimple Employee data
     * @return The created configurateur
     */
    @Transactional
    public Configurateur createConfigurateur(Configurateur configurateur, EmployeSimple employeSimple) {
        // Set role for employee
        Role role = roleRepository.findByIdRole("CONFIGURATEUR")
                .orElseThrow(() -> new RuntimeException("Role CONFIGURATEUR not found"));
        employeSimple.setRole(role);

        // Set password with proper encoding
        employeSimple.setMotDePasse(passwordEncoder.encode(employeSimple.getMotDePasse()));

        // Generate ID if not provided
        if (employeSimple.getIdEmploye() == null) {
            employeSimple.setIdEmploye(UUID.randomUUID().toString());
        }

        // Save employee first
        employeRepository.save(employeSimple);

        // Configure the configurateur
        configurateur.setIdConfiguration(employeSimple.getIdEmploye());
        configurateur.setDateCreation(new Timestamp(System.currentTimeMillis()));
        configurateur.setDateModification(new Timestamp(System.currentTimeMillis()));

        return configurateurRepository.save(configurateur);
    }
    
    /**
     * Update an existing configurateur
     * @param id Configurateur ID
     * @param configurateur Updated configurateur data
     * @return The updated configurateur
     */
    @Transactional
    public Configurateur updateConfigurateur(String id, Configurateur configurateur) {
        Configurateur existingConfigurateur = configurateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configurateur not found with id: " + id));
        
        // Update basic fields
        existingConfigurateur.setNom(configurateur.getNom());
        existingConfigurateur.setPrenom(configurateur.getPrenom());
        existingConfigurateur.setEmail(configurateur.getEmail());
        existingConfigurateur.setTelephone(configurateur.getTelephone());
        existingConfigurateur.setNomUtilisateur(configurateur.getNomUtilisateur());
        existingConfigurateur.setEtatCompte(configurateur.getEtatCompte());
        existingConfigurateur.setDateModification(new Timestamp(System.currentTimeMillis()));
        
        return configurateurRepository.save(existingConfigurateur);
    }
    
    /**
     * Delete a configurateur
     * @param id Configurateur ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteConfigurateur(String id) {
        if (configurateurRepository.existsById(id)) {
            configurateurRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Authenticate a configurateur
     * @param email Configurateur email
     * @param motDePasse Configurateur password
     * @return The configurateur if authentication succeeds, null otherwise
     */
    public Configurateur authenticate(String email, String motDePasse) {
        Configurateur configurateur = configurateurRepository.findByEmail(email)
            .orElse(null);
        
        if (configurateur != null && configurateur.getEmployeSimple() != null) {
            String storedPassword = configurateur.getEmployeSimple().getMotDePasse();
            if (passwordEncoder.matches(motDePasse, storedPassword)) {
                return configurateur;
            }
        }
        
        return null;
    }
    
    /**
     * Change configurateur password
     * @param id Configurateur ID
     * @param ancienMotDePasse Old password
     * @param nouveauMotDePasse New password
     * @return true if password changed, false otherwise
     */
    @Transactional
    public boolean changePassword(String id, String ancienMotDePasse, String nouveauMotDePasse) {
        Configurateur configurateur = configurateurRepository.findById(id)
            .orElse(null);
            
        if (configurateur != null && configurateur.getEmployeSimple() != null) {
            EmployeSimple employe = configurateur.getEmployeSimple();
            
            // Check if old password matches
            if (passwordEncoder.matches(ancienMotDePasse, employe.getMotDePasse())) {
                // Set new password
                employe.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
                employeRepository.save(employe);
                
                // Update modification date
                configurateur.setDateModification(new Timestamp(System.currentTimeMillis()));
                configurateurRepository.save(configurateur);
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Reset configurateur password
     * @param id Configurateur ID
     * @return Generated password or null if configurateur not found
     */
    @Transactional
    public String resetPWD(String id) {
        Configurateur configurateur = configurateurRepository.findById(id)
            .orElse(null);
            
        if (configurateur != null && configurateur.getEmployeSimple() != null) {

            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            

            EmployeSimple employe = configurateur.getEmployeSimple();
            employe.setMotDePasse(passwordEncoder.encode(tempPassword));
            employeRepository.save(employe);
            

            configurateur.setDateModification(new Timestamp(System.currentTimeMillis()));
            configurateurRepository.save(configurateur);
            
            return tempPassword;
        }
        
        return null;
    }
}