package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@Entity
public class Rh {

    @Id
    private String idEmploye;

    private String nom;
    private String prenom;
    private String email;
    private Date dateCreation;
    private Date dateModification;

    @OneToOne
    @JoinColumn(name = "id_employe", referencedColumnName = "idEmploye", insertable = false, updatable = false)
    private EmployeSimple employeSimple;

}

