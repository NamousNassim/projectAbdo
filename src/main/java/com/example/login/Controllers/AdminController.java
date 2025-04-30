package com.example.login.Controllers;

import com.example.login.Models.*;
import com.example.login.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Ensures only admins can access these endpoints
public class AdminController {

    private final ConfigurateurService configurateurService;
    private final RhService rhService;
    private final AuthenticationService authService;

    @Autowired
    public AdminController(
            ConfigurateurService configurateurService,
            RhService rhService,
            AuthenticationService authService) {
        this.configurateurService = configurateurService;
        this.rhService = rhService;
        this.authService = authService;
    }

    // DTO class to hold both employee and specific role data
    public static class UserRegistrationDto {
        private EmployeSimple employeSimple;
        private Object specificData; // Can be Configurateur, Rh, etc.

        public EmployeSimple getEmployeSimple() {
            return employeSimple;
        }

        public void setEmployeSimple(EmployeSimple employeSimple) {
            this.employeSimple = employeSimple;
        }

        public Object getSpecificData() {
            return specificData;
        }

        public void setSpecificData(Object specificData) {
            this.specificData = specificData;
        }
    }

    @PostMapping("/add-configurateur")
    public ResponseEntity<?> addConfigurateur(@RequestBody UserRegistrationDto registrationDto) {
        try {
            Configurateur configurateur = new Configurateur();
            configurateur.setNom(registrationDto.getEmployeSimple().getNom());
            configurateur.setPrenom(registrationDto.getEmployeSimple().getPrenom());
            configurateur.setEmail(registrationDto.getEmployeSimple().getEmailPro());

            Configurateur saved = configurateurService.createConfigurateur(configurateur, registrationDto.getEmployeSimple());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating configurateur: " + e.getMessage());
        }
    }

    @PostMapping("/add-rh")
    public ResponseEntity<?> addRh(@RequestBody UserRegistrationDto registrationDto) {
        try {
            Rh rh = new Rh();
            rh.setNom(registrationDto.getEmployeSimple().getNom());
            rh.setPrenom(registrationDto.getEmployeSimple().getPrenom());
            rh.setEmail(registrationDto.getEmployeSimple().getEmailPro());

            Rh saved = rhService.createRh(rh, registrationDto.getEmployeSimple());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating RH: " + e.getMessage());
        }
    }

    @PostMapping("/add-simple-employee")
    public ResponseEntity<?> addSimpleEmployee(@RequestBody EmployeSimple employeSimple) {
        try {
            authService.registerUser(employeSimple);
            return ResponseEntity.ok("Employee created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating employee: " + e.getMessage());
        }
    }
}