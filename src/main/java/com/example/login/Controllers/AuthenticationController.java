package com.example.login.Controllers;

import com.example.login.Models.EmployeSimple;
import com.example.login.Services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AuthenticationManager authenticationManager) {
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok("Connexion réussie !");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Échec de la connexion: Email ou mot de passe incorrect.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid EmployeSimple employeSimple) {
        try {
            authenticationService.registerUser(employeSimple);
            return ResponseEntity.ok("Inscription réussie !");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }
}

