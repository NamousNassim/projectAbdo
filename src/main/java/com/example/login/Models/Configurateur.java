package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
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
    private EmployeSimple employeSimple;


}
