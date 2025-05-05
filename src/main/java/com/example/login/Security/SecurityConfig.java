package com.example.login.Security;

import com.example.login.Models.Role;
import com.example.login.Repositories.EmployeSimpleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final EmployeSimpleRepository employeSimpleRepository;

    public SecurityConfig(EmployeSimpleRepository employeSimpleRepository) {
        this.employeSimpleRepository = employeSimpleRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define encoders map with bcrypt and noop (plain text) encoders
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        
        // Create delegating encoder with bcrypt as default for encoding new passwords
        DelegatingPasswordEncoder delegatingEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        
        // Critical line: set default encoder for passwords without a prefix
        delegatingEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        
        return delegatingEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/login", "/api/register", "/api/configurateurs/authenticate").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/setup/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            System.out.println("Loading user: " + email);
            return employeSimpleRepository.findByEmailProWithRole(email)
                    .or(() -> employeSimpleRepository.findByEmailPersoWithRole(email))
                    .map(employeSimple -> {
                        Role role = employeSimple.getRole();
                        String roleName = role != null ? role.getNomRole() : "USER";
                        
                        String password = employeSimple.getMotDePasse();
                        // Handle password prefixes correctly
                        if (password != null) {
                            // If password has {noop} prefix, keep it as is
                            if (password.startsWith("{noop}")) {
                                // No changes needed
                            }
                            // If no prefix and it looks like bcrypt (starts with $2), add bcrypt prefix
                            else if (!password.startsWith("{") && password.startsWith("$2")) {
                                password = "{bcrypt}" + password;
                            }
                            // If no prefix and not bcrypt format, treat as plain text
                            else if (!password.startsWith("{")) {
                                password = "{noop}" + password;
                            }
                        }
                        
                        System.out.println("Found user: " + employeSimple.getEmailPro() + " with role: " + roleName);
                        return User.builder()
                                .username(employeSimple.getEmailPro())
                                .password(password)
                                .roles(roleName)
                                .build();
                    })
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }
}