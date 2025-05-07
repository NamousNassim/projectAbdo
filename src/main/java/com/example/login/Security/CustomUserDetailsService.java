package com.example.login.Security;

import com.example.login.Repositories.EmployeSimpleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeSimpleRepository employeSimpleRepository;

    @Autowired
    public CustomUserDetailsService(EmployeSimpleRepository employeSimpleRepository) {
        this.employeSimpleRepository = employeSimpleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}