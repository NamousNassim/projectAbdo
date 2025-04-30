package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Getter
@Setter
@Entity
public class Configurateur {

    @Id
    private String idConfiguration;

    private String nom;
    private String prenom;
    private String email;
    private Timestamp dateModification;

    @OneToOne
    @JoinColumn(name = "id_Configuration", referencedColumnName = "idEmploye", insertable = false, updatable = false)
     @JsonIgnoreProperties({"administrateur", "configurateur", "rh"})
    private EmployeSimple employeSimple;


}
