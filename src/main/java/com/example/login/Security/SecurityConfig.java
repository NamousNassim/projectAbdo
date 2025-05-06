package com.example.login.Security;

import com.example.login.Repositories.EmployeSimpleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final EmployeSimpleRepository employeSimpleRepository;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(EmployeSimpleRepository employeSimpleRepository) {
        this.employeSimpleRepository = employeSimpleRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        DelegatingPasswordEncoder delegatingEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        delegatingEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegatingEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/login", "/api/register", "/api/configurateurs/authenticate").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/setup/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            return employeSimpleRepository.findByEmailProWithRole(email)
                    .or(() -> employeSimpleRepository.findByEmailPersoWithRole(email))
                    .map(employeSimple -> {
                        var role = employeSimple.getRole();
                        String roleName = role != null ? role.getNomRole() : "USER";
                        String password = employeSimple.getMotDePasse();
                        if (password != null) {
                            if (password.startsWith("{noop}")) {
                                // No changes needed
                            } else if (!password.startsWith("{") && password.startsWith("$2")) {
                                password = "{bcrypt}" + password;
                            } else if (!password.startsWith("{")) {
                                password = "{noop}" + password;
                            }
                        }
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