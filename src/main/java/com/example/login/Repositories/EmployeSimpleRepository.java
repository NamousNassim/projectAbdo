package com.example.login.Repositories;

import com.example.login.Models.EmployeSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeSimpleRepository extends JpaRepository<EmployeSimple, String> {
    Optional<EmployeSimple> findByEmailPro(String emailPro);
    Optional<EmployeSimple> findByEmailPerso(String email);
    
    // Add this method to handle multiple results
    List<EmployeSimple> findAllByEmailPro(String emailPro);
    
    @Query("SELECT e FROM EmployeSimple e JOIN FETCH e.role WHERE e.emailPro = :email")
    Optional<EmployeSimple> findByEmailProWithRole(@Param("email") String email);
    
    @Query("SELECT e FROM EmployeSimple e JOIN FETCH e.role WHERE e.emailPerso = :email")
    Optional<EmployeSimple> findByEmailPersoWithRole(@Param("email") String email);
}