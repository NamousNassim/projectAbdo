package com.example.login.Controllers;

import com.example.login.Controllers.AdminController.UserRegistrationDto;
import com.example.login.Models.Configurateur;
import com.example.login.Models.EmployeSimple;
import com.example.login.Services.ConfigurateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/configurateurs")
public class ConfigurateurController {

    private final ConfigurateurService configurateurService;

    @Autowired
    public ConfigurateurController(ConfigurateurService configurateurService) {
        this.configurateurService = configurateurService;
    }

    /**
     * Get all configurateurs
     * @return List of all configurateurs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Configurateur>> getAllConfigurateurs() {
        return ResponseEntity.ok(configurateurService.listConfigurateurs());
    }

    /**
     * Get configurateur by ID
     * @param id Configurateur ID
     * @return The configurateur if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> getConfigurateur(@PathVariable String id) {
        Configurateur configurateur = configurateurService.getConfigurateurById(id);
        if (configurateur == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(configurateur);
    }

    /**
     * Create a new configurateur
     * @param registrationDto Registration data containing employee and configurateur info
     * @return The created configurateur
     */
 @PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> createConfigurateur(@RequestBody UserRegistrationDto registrationDto) {
    try {
        // Generate a unique ID for the configurateur/employee
        String uniqueId = UUID.randomUUID().toString();
        
        // Set up the employee
        EmployeSimple employeSimple = registrationDto.getEmployeSimple();
        employeSimple.setIdEmploye(uniqueId);
        
        // Set up the configurateur
        Configurateur configurateur = new Configurateur();
        configurateur.setIdConfigurateur(uniqueId); // Set ID explicitly
        configurateur.setNom(employeSimple.getNom());
        configurateur.setPrenom(employeSimple.getPrenom());
        configurateur.setEmail(employeSimple.getEmailPro());
        
        // Add data from specificData if available
        if (registrationDto.getSpecificData() != null) {
            Map<String, Object> specificData = (Map<String, Object>) registrationDto.getSpecificData();
            if (specificData.get("telephone") != null) {
                configurateur.setTelephone(specificData.get("telephone").toString());
            } else {
                configurateur.setTelephone(employeSimple.getTelephone() != null ? 
                    employeSimple.getTelephone().toString() : null);
            }
            if (specificData.get("nomUtilisateur") != null) {
                configurateur.setNomUtilisateur(specificData.get("nomUtilisateur").toString());
            } else {
                configurateur.setNomUtilisateur(employeSimple.getNomUtilisateur());
            }
            if (specificData.get("etatCompte") != null) {
                configurateur.setEtatCompte(specificData.get("etatCompte").toString());
            } else {
                configurateur.setEtatCompte("ACTIF");
            }
        } else {
            configurateur.setTelephone(employeSimple.getTelephone() != null ? 
                employeSimple.getTelephone().toString() : null);
            configurateur.setNomUtilisateur(employeSimple.getNomUtilisateur());
            configurateur.setEtatCompte("ACTIF");
        }

        Configurateur saved = configurateurService.createConfigurateur(configurateur, employeSimple);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error creating configurateur: " + e.getMessage());
    }
}

    /**
     * Update an existing configurateur
     * @param id Configurateur ID
     * @param configurateur Updated configurateur data
     * @return The updated configurateur
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateConfigurateur(@PathVariable String id, @RequestBody Configurateur configurateur) {
        try {
            Configurateur updated = configurateurService.updateConfigurateur(id, configurateur);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating configurateur: " + e.getMessage());
        }
    }

    /**
     * Delete a configurateur
     * @param id Configurateur ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteConfigurateur(@PathVariable String id) {
        boolean deleted = configurateurService.deleteConfigurateur(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Authenticate a configurateur
     * @param credentials Credentials containing email and password
     * @return The configurateur if authentication succeeds
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String motDePasse = credentials.get("motDePasse");
        
        if (email == null || motDePasse == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        
        Configurateur configurateur = configurateurService.authenticate(email, motDePasse);
        if (configurateur == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        
        return ResponseEntity.ok(configurateur);
    }

    /**
     * Change configurateur password
     * @param id Configurateur ID
     * @param passwords Map containing old and new passwords
     * @return Success message if password changed
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@PathVariable String id, @RequestBody Map<String, String> passwords) {
        String ancienMotDePasse = passwords.get("ancienMotDePasse");
        String nouveauMotDePasse = passwords.get("nouveauMotDePasse");
        
        if (ancienMotDePasse == null || nouveauMotDePasse == null) {
            return ResponseEntity.badRequest().body("Old and new passwords are required");
        }
        
        boolean changed = configurateurService.changePassword(id, ancienMotDePasse, nouveauMotDePasse);
        if (changed) {
            return ResponseEntity.ok("Password changed successfully");
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password");
    }

    /**
     * Reset configurateur password
     * @param id Configurateur ID
     * @return The new temporary password
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable String id) {
        String tempPassword = configurateurService.resetPWD(id);
        if (tempPassword == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of("temporaryPassword", tempPassword));
    }
}