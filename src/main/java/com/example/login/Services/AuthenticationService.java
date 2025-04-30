package com.example.login.Services;

import com.example.login.Models.EmployeSimple;
import com.example.login.Repositories.EmployeSimpleRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final EmployeSimpleRepository employeSimpleRepository;

    public AuthenticationService(EmployeSimpleRepository employeSimpleRepository) {
        this.employeSimpleRepository = employeSimpleRepository;
    }

    public void registerUser(EmployeSimple employeSimple) {
        // Ajouter {noop} pour indiquer que le mot de passe est en texte clair
        employeSimple.setMotDePasse("{noop}" + employeSimple.getMotDePasse());
        employeSimpleRepository.save(employeSimple);
    }
}
