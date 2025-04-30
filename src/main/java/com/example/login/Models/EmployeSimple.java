package com.example.login.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@Entity
public class EmployeSimple {

    @Id
    private String idEmploye;
    
    // Other fields remain the same

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role", referencedColumnName = "idRole")
    private Role role;

    @OneToOne(mappedBy = "employeSimple", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("employeSimple")
    private Rh rh;

    @OneToOne(mappedBy = "employeSimple", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("employeSimple")
    private Configurateur configurateur;

    @OneToOne(mappedBy = "employeSimple", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("employeSimple")
    private Administrateur administrateur;
}